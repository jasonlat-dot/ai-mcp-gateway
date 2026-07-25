package com.jasonlat.ai.infrastructure.adapter.port.tool;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO.DubboConfig;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO.ProtocolMapping;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dubbo 协议工具调用器(Generic 模式)。
 * <p>
 * <b>核心设计:不依赖任何 Provider 接口 jar。</b>
 * 通过 Dubbo 官方的 {@link GenericService} + {@code $invoke} 方法,可以用接口名 + 方法名 +
 * 参数类型字符串 调任意 Dubbo 服务,无需本地 classpath 持有接口 Class。
 * <p>
 * 为什么不依赖 dubbo-api?
 * <ul>
 *   <li>主工程是通用 MCP 网关,今天调员工查询,明天调订单、支付 — 依赖具体业务 jar 会污染主工程</li>
 *   <li>HTTP 协议天然不用依赖(只关心 url/body 字符串),Dubbo 也应该做到一样</li>
 *   <li>GenericService 是 Dubbo 官方提供的"通用调用"通道,生产可用</li>
 * </ul>
 * <p>
 * 为什么不依赖 Dubbo 自带的 {@code ReferenceConfigCache}?
 * <ul>
 *   <li>Dubbo 3.2.x 把 API 换成了 {@code SimpleReferenceCache},3.3.x 又在变 — 与框架内部类耦合度高</li>
 *   <li>GenericService 代理本身线程安全、可复用,自己用 Caffeine 缓存更可控,顺带做 TTL</li>
 * </ul>
 * <p>
 * 调用流程:
 * <ol>
 *   <li>从 {@link McpToolProtocolConfigVO.DubboConfig} 取出 interface/group/version/method</li>
 *   <li>按 (interface + group + version) 查缓存,没有则 new ReferenceConfig 并 init</li>
 *   <li>把 LLM 传入的参数 Map 包成 {@code Object[]}(按 parameterTypes 顺序对齐),作为 $invoke 的 args</li>
 *   <li>调用 genericService.$invoke(methodName, parameterTypes, args),返回结果转 JSON 字符串</li>
 * </ol>
 *
 * @author jasonlat
 */
@Slf4j
@Component
public class DubboInvoker {

    /**
     * ReferenceConfig 缓存:key=interfaceName#group#version,value=ReferenceConfig#GenericService#。
     * <p>
     * GenericService 代理线程安全,同一组 (interface, group, version) 复用同一代理即可。
     * <p>
     * 使用 Caffeine 实现过期回收:写后 30 分钟未访问即过期,下次访问时重建。
     * 避免 Nacos Provider 实例变更后旧代理长期残留、连接地址陈旧。
     */
    private static final Duration REFERENCE_CACHE_TTL = Duration.ofMinutes(30);

    private final Cache<String, ReferenceConfig<GenericService>> referenceCache = Caffeine.newBuilder()
            .expireAfterAccess(REFERENCE_CACHE_TTL)
            .maximumSize(500)
            .removalListener((key, value, cause) -> {
                if (value instanceof ReferenceConfig<?> ref) {
                    try {
                        ref.destroy();
                    } catch (Exception e) {
                        log.warn("[DubboInvoker] auto-destroy reference failed: {} cause={}", key, e.getMessage());
                    }
                }
            })
            .build();

    /**
     * Dubbo 远程调用(Generic 模式)。
     * <p>
     * 调用流程:
     * <ol>
     *   <li>解析直连 URL 列表(来自 cfg 或系统属性兜底);为空则走 Nacos 模式</li>
     *   <li>按 mapping 把 params 拆成 Object[] args(对每个 URL 复用,避免重试时重算)</li>
     *   <li>直连模式:按 URL 顺序故障转移,首个成功即返回;全部失败抛错</li>
     *   <li>Nacos 模式:直接 $invoke,失败就抛错(不重试,重试交给 Dubbo 自身 retries)</li>
     * </ol>
     *
     * @param protocolConfig 完整协议配置
     * @param params         MCP 客户端传入的参数(可能是 Map,也可能是别的结构)
     * @return JSON 字符串形式的 Dubbo 返回值
     */
    public Object invoke(McpToolProtocolConfigVO protocolConfig, Object params) {
        DubboConfig cfg = protocolConfig.getDubboConfig();
        if (cfg == null) {
            throw new IllegalArgumentException("dubboConfig is null, protocolType mismatch?");
        }

        // 1. 提前算好 args,故障转移的多次调用复用同一份参数(避免 mcpPath 解包每次都跑)
        Object[] args = buildArgs(
                cfg.getParameterTypes(),
                params,
                protocolConfig.getRequestProtocolMappings()
        );

        // 2. 解析直连 URL 列表
        List<String> directUrls = resolveDirectUrls(cfg);

        if (directUrls.isEmpty()) {
            // Nacos 模式:单次调用
            log.debug("[DubboInvoker] $invoke {}.{} via Nacos", cfg.getInterfaceName(), cfg.getMethodName());
            return doInvoke(cfg, args, null);
        }

        // 3. 直连模式:按顺序故障转移
        log.info("[DubboInvoker] $invoke {}.{} via DIRECT chain ({} URL(s)): {}",
                cfg.getInterfaceName(), cfg.getMethodName(),
                directUrls.size(), directUrls);

        List<Throwable> failures = new ArrayList<>();
        for (String url : directUrls) {
            try {
                return doInvoke(cfg, args, url);
            } catch (RuntimeException e) {
                if (isFailoverCandidate(e)) {
                    log.warn("[DubboInvoker] direct URL [{}] failed: {}, try next", url, e.getMessage());
                    failures.add(e);
                    continue;
                }
                // 非故障转移类异常(参数错误、序列化错误):立即抛,不要浪费后面 URL 的预算
                throw e;
            }
        }

        // 4. 全部失败
        log.error("[DubboInvoker] all {} direct URL(s) failed for {}.{}: {}",
                directUrls.size(), cfg.getInterfaceName(), cfg.getMethodName(),
                failures.stream().map(Throwable::getMessage).collect(Collectors.joining(" | ")));
        throw new RuntimeException(
                "all direct URLs failed [" + cfg.getInterfaceName() + "." + cfg.getMethodName() + "]: "
                        + failures.stream().map(Throwable::getMessage).collect(Collectors.joining(" | ")),
                failures.isEmpty() ? null : failures.get(failures.size() - 1));
    }

    /**
     * 单次 $invoke 调用,带统一异常包装。
     * <p>
     * singleUrl 为 null 时走 Nacos,非 null 时只直连该 URL(与故障转移解耦)。
     */
    private Object doInvoke(DubboConfig cfg, Object[] args, String singleUrl) {
        try {
            GenericService genericService = getOrCreateGeneric(cfg, singleUrl);

            log.debug("[DubboInvoker] $invoke {}.{} parameterTypes={} args={}",
                    cfg.getInterfaceName(), cfg.getMethodName(),
                    JSON.toJSONString(cfg.getParameterTypes()),
                    JSON.toJSONString(args));

            Object result = genericService.$invoke(
                    cfg.getMethodName(),
                    cfg.getParameterTypes() == null ? null : cfg.getParameterTypes().toArray(new String[0]),
                    args
            );

            // Provider 端已经把 POJO 序列化成 Map(POJO 模式),或保持原值(Map 模式)
            String json = JSON.toJSONString(result);
            log.debug("[DubboInvoker] $invoke {}.{} success, result={}",
                    cfg.getInterfaceName(), cfg.getMethodName(), json);
            return json;

        } catch (RpcException e) {
            log.error("[DubboInvoker] dubbo rpc failed: {}#{} url={} {}", cfg.getInterfaceName(), cfg.getMethodName(), singleUrl == null ? "Nacos" : singleUrl, e.getMessage(), e);
            throw new RuntimeException("dubbo call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[DubboInvoker] invoke error: {}#{} url={} {}",
                    cfg.getInterfaceName(), cfg.getMethodName(),
                    singleUrl == null ? "Nacos" : singleUrl, e.getMessage(), e);
            throw new RuntimeException("dubbo invoke error: " + e.getMessage(), e);
        }
    }

    /**
     * 获取或创建一个 GenericService 代理。
     * <p>
     * 单 URL 维度缓存:每次调用传一个明确的 singleUrl(直连)或 null(Nacos)。
     * 多 URL 场景下,URL 列表由 {@link #invoke} 遍历,每次调用进入这里都是单一 URL。
     * <p>
     * 关键点:setInterface 用 String(接口全限定名)+ setGeneric(true) — 不需要 Class<?>。
     * <p>
     * 注:ReferenceConfig 的 init() 是懒加载触发的(在第一次 get() 时自动执行),
     * 所以这里只需要 set 各种配置,不需要显式调用 init。
     */
    private GenericService getOrCreateGeneric(DubboConfig dubboConfig, String singleUrl) {
        String cacheKey = buildCacheKey(dubboConfig, singleUrl);
        return referenceCache.get(cacheKey, k -> createReference(dubboConfig, singleUrl)).get();
    }

    /**
     * 真正构造 ReferenceConfig 的工厂方法。
     * <p>
     * 单独抽出来是为了让 getter lambda 干净 — 不读 cfg 上的字段,只读参数。
     */
    private ReferenceConfig<GenericService> createReference(DubboConfig cfg, String singleUrl) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setInterface(cfg.getInterfaceName());        // 关键:String,无需 Class.forName
        reference.setGeneric("true");                          // 关键:开启泛化调用
        reference.setGroup(cfg.getGroup());
        reference.setVersion(cfg.getVersion());
        reference.setTimeout(cfg.getTimeoutMs() == null ? 5000 : cfg.getTimeoutMs());
        reference.setRetries(cfg.getRetries() == null ? 0 : cfg.getRetries());
        reference.setCheck(false);                             // 启动时不强制要求 Provider 在线
        reference.setLazy(true);                               // 懒加载,首次调用时再连接 Nacos
        if (StringUtils.isNotBlank(singleUrl)) {
            // 直连本 URL,绕过 Nacos
            log.debug("[DubboInvoker] create DIRECT reference: url={}", singleUrl);
            reference.setUrl(singleUrl);
        }
        return reference;
    }

    /**
     * 判断异常是否值得故障转移到下一个 URL。
     * <p>
     * 仅网络/超时/注册中心/无可用 invoker 等"实例不通"类异常才重试;
     * 参数序列化错误、Provider 业务异常(被 try-catch 抛出后会带业务码)立即抛,不要浪费后续 URL。
     */
    private boolean isFailoverCandidate(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            if (cur instanceof RpcException rpc) {
                int code = rpc.getCode();
                return code == RpcException.NETWORK_EXCEPTION
                        || code == RpcException.TIMEOUT_EXCEPTION
                        || code == RpcException.REGISTRY_EXCEPTION
                        || code == RpcException.NO_INVOKER_AVAILABLE_AFTER_FILTER
                        || code == RpcException.FORBIDDEN_EXCEPTION
                        || code == 0;  // 0: 部分 SerializationException 或初始化异常
            }
            cur = cur.getCause();
        }
        return false;
    }

    /**
     * 把 LLM 传入的参数组装成 $invoke 用的 Object[]。
     * <p>
     * 关键转换:LLM 给的参数是 MCP 包装结构(有 wrapper 节点如 xxxRequest01),
     * Dubbo Provider 期望的是 POJO 扁平结构。需要按 mapping 把 wrapper 剥掉。
     * <p>
     * 策略:
     * <ul>
     *   <li>parameterTypes 为空 → 返回整个 params 当成 args[0](让 Provider 自己推断类型)</li>
     *   <li>parameterTypes.length == 1 → 按 mapping 把 params 转换为 POJO 等价的 Map,作为 args[0]</li>
     *   <li>parameterTypes.length > 1 → 多参数,按 params 是 List 顺序取值,每个元素再按 mapping 转换</li>
     * </ul>
     *
     * @param parameterTypes     参数类型全限定名列表(来自 DubboConfig)
     * @param params             LLM 传入的参数(Map 或 List 或 POJO)
     * @param requestMappings    字段映射表(用于解包 wrapper)
     */
    private Object[] buildArgs(List<String> parameterTypes, Object params,
                               List<ProtocolMapping> requestMappings) {
        // parameterTypes 为空:无类型信息,把整个 params 当成 args[0]
        if (parameterTypes == null || parameterTypes.isEmpty()) {
            return new Object[]{params};
        }

        // 单参数场景(常见)
        if (parameterTypes.size() == 1) {
            Object arg = convertToPojoMap(params, requestMappings, parameterTypes.get(0));
            return new Object[]{arg};
        }

        // 多参数场景:params 必须是 List<Object>,顺序与 parameterTypes 对齐
        if (params instanceof List<?> paramList) {
            Object[] arr = new Object[parameterTypes.size()];
            for (int i = 0; i < parameterTypes.size(); i++) {
                Object p = i < paramList.size() ? paramList.get(i) : null;
                arr[i] = convertToPojoMap(p, requestMappings, parameterTypes.get(i));
            }
            return arr;
        }

        // 多参数但 params 不是 List:无法对齐,降级用整个 Map 当成 args[0],其余参数为 null
        log.warn("[DubboInvoker] multi-param but params is not List ({}), degrade to first arg only",
                params == null ? "null" : params.getClass().getSimpleName());
        Object arg = convertToPojoMap(params, requestMappings, parameterTypes.get(0));
        return new Object[]{arg, null, null, null};
    }

    /**
     * 把 LLM 传入的参数转换为 POJO 等价的 Map。
     * <p>
     * 核心:mcp_path 里如果有 wrapper 节点(对应 mcp_protocol_mapping 的 root fieldName),
     * 把它剥掉,只保留 POJO 实际字段。
     * <p>
     * 例:
     * <pre>
     *   params = { "xxxRequest01": { "city": "beijing", "company": { "name": "jd", "type": "互联网" } } }
     *   mappings 中 root 节点 fieldName = "xxxRequest01"
     *   → 解包后: { "city": "beijing", "company": { "name": "jd", "type": "互联网" } }
     *   → Provider 端 Hessian2 按 EmployeeRequest 反序列化,字段对齐
     * </pre>
     *
     * @param params        LLM 传入的参数
     * @param mappings      request 类型映射表(可能为 null — 此时不做解包)
     * @param parameterType 参数类型全限定名(用于日志/debug)
     * @return POJO 等价的 Map
     */
    @SuppressWarnings("unchecked")
    private Object convertToPojoMap(Object params, List<ProtocolMapping> mappings, String parameterType) {
        Object normalized = normalizeArg(params);
        if (!(normalized instanceof Map) || mappings == null || mappings.isEmpty()) {
            // 没 mapping 就原样返回
            return normalized;
        }

        Map<String, Object> paramMap = (Map<String, Object>) normalized;

        // 找 root 节点(parent_path IS NULL)
        ProtocolMapping root = findRootMapping(mappings);
        if (root == null) {
            return paramMap;
        }

        // 如果 params 的 root key 存在,解包
        if (paramMap.containsKey(root.getFieldName())) {
            Object inner = paramMap.get(root.getFieldName());
            if (inner instanceof Map) {
                log.debug("[DubboInvoker] unwrap root wrapper [{}] for type {}", root.getFieldName(), parameterType);
                return inner;
            }
        }

        // params 里没找到 wrapper key,说明 params 已经是 POJO 扁平结构,直接返回
        return paramMap;
    }

    /**
     * 在 mappings 中找到 parent_path 为 NULL 的节点(通常只有 1 个,对应 xxxRequest0X)。
     * 如果有多个,取第一个。
     */
    private ProtocolMapping findRootMapping(List<ProtocolMapping> mappings) {
        for (ProtocolMapping m : mappings) {
            if ("request".equals(m.getMappingType())
                    && (m.getParentPath() == null || m.getParentPath().isBlank())) {
                return m;
            }
        }
        return null;
    }

    /**
     * 把入参统一成 Dubbo Generic 接受的格式(JavaBean / Map)。
     * <p>
     * - 如果 params 已经是 Map,直接返回
     * - 如果 params 是 POJO,转成 Map(避免序列化方式不一致)
     */
    @SuppressWarnings("unchecked")
    private Object normalizeArg(Object params) {
        if (params == null) {
            return new HashMap<>();
        }
        if (params instanceof Map) {
            return params;
        }
        // 用 fastjson2 转成 Map(JSON 路线,确保 POJO 的所有字段都能被 Provider 端读到)
        String json = JSON.toJSONString(params);
        return JSON.parseObject(json, Map.class);
    }

    /**
     * 构造 ReferenceConfig 缓存 key。
     * <p>
     * - 直连模式:key = "DIRECT:singleUrl",URL 唯一决定引用身份;
     *   不同 URL 在多实例场景下会得到不同的缓存项,各自独立。
     * - Nacos 模式:key = "interfaceName#group#version",同一组配置复用同一代理。
     */
    private String buildCacheKey(DubboConfig dubboConfig, String singleUrl) {
        if (StringUtils.isNotBlank(singleUrl)) {
            return "DIRECT:" + singleUrl;
        }
        return dubboConfig.getInterfaceName() + "#"
                + (dubboConfig.getGroup() == null ? "" : dubboConfig.getGroup()) + "#"
                + (dubboConfig.getVersion() == null ? "" : dubboConfig.getVersion());
    }

    /**
     * 解析直连 URL 列表(逗号分隔已经由 Repository 拆好,这里直接读)。
     * <p>
     * 优先级:
     * 1) cfg.directEnabled=true 且 cfg.directUrls 非空 → 返回 cfg.directUrls
     * 2) 系统属性 dubbo.direct.url 兜底(逗号分隔,trim 过滤)
     * 3) 都没有 → 空列表(走 Nacos 模式)
     */
    private List<String> resolveDirectUrls(DubboConfig dubboConfig) {
        if (Boolean.TRUE.equals(dubboConfig.getDirectEnabled())
                && dubboConfig.getDirectUrls() != null
                && !dubboConfig.getDirectUrls().isEmpty()) {
            return dubboConfig.getDirectUrls();
        }
        String sysProp = System.getProperty("dubbo.direct.url");
        if (StringUtils.isNotBlank(sysProp)) {
            return Arrays.stream(sysProp.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 容器销毁时关闭所有 ReferenceConfig,释放连接。
     */
    @PreDestroy
    public void destroy() {
        Map<String, ReferenceConfig<GenericService>> snapshot = referenceCache.asMap();
        log.debug("[DubboInvoker] destroying {} cached references", snapshot.size());
        for (Map.Entry<String, ReferenceConfig<GenericService>> entry : snapshot.entrySet()) {
            try {
                entry.getValue().destroy();
            } catch (Exception e) {
                log.warn("[DubboInvoker] destroy reference [{}] failed: {}", entry.getKey(), e.getMessage());
            }
        }
        referenceCache.invalidateAll();
        referenceCache.cleanUp();
    }
}
