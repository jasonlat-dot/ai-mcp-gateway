package com.jasonlat.ai.infrastructure.adapter.port.tool;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO.DubboConfig;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO.ProtocolMapping;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 *   <li>GenericService 代理本身线程安全、可复用,自己用 {@link ConcurrentHashMap} 缓存更可控</li>
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
     * ReferenceConfig 缓存:key=interfaceName#group#version,value=ReferenceConfig&lt;GenericService&gt;。
     * <p>
     * GenericService 代理线程安全,同一组 (interface, group, version) 复用同一代理即可。
     */
    private final Map<String, ReferenceConfig<GenericService>> referenceCache = new ConcurrentHashMap<>();
    // todo 1. 缓存似乎没必要？因为只用到了 没用从中缓存中取数据。
    // todo 2. 当前仅支持nacos的格式，是否需要适配直连？直连网关又是否需要加入nacos?

    /**
     * Dubbo 远程调用(Generic 模式)。
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

        try {
            // 1. 获取/创建 GenericService 引用
            GenericService genericService = getOrCreateGeneric(cfg);

            // 2. 把 params 组装成 $invoke 用的 Object[]
            //    按 mapping 解包 wrapper(如 xxxRequest01),让 Provider 端拿到 POJO 扁平结构
            Object[] args = buildArgs(
                    cfg.getParameterTypes(),
                    params,
                    protocolConfig.getRequestProtocolMappings()
            );

            // 3. 调用
            log.info("[DubboInvoker] $invoke {}.{} parameterTypes={} args={}",
                    cfg.getInterfaceName(), cfg.getMethodName(),
                    JSON.toJSONString(cfg.getParameterTypes()),
                    JSON.toJSONString(args));

            Object result = genericService.$invoke(
                    cfg.getMethodName(),
                    cfg.getParameterTypes() == null ? null : cfg.getParameterTypes().toArray(new String[0]),
                    args
            );

            // 4. 返回结果序列化。Provider 端已经把 POJO 序列化成 Map(POJO 模式),或保持原值(Map 模式)
            String json = JSON.toJSONString(result);
            log.info("[DubboInvoker] $invoke {}.{} success, result={}",
                    cfg.getInterfaceName(), cfg.getMethodName(), json);
            return json;

        } catch (RpcException e) {
            log.error("[DubboInvoker] dubbo rpc failed: {}#{} {}",
                    cfg.getInterfaceName(), cfg.getMethodName(), e.getMessage(), e);
            throw new RuntimeException("dubbo call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[DubboInvoker] invoke error: {}#{}", cfg.getInterfaceName(), cfg.getMethodName(), e);
            throw new RuntimeException("dubbo invoke error: " + e.getMessage(), e);
        }
    }

    /**
     * 获取或创建一个 GenericService 代理。
     * <p>
     * 关键点:setInterface 用 String(接口全限定名)+ setGeneric(true) — 不需要 Class<?>。
     * <p>
     * 注:ReferenceConfig 的 init() 是懒加载触发的(在第一次 get() 时自动执行),
     * 所以这里只需要 set 各种配置,不需要显式调用 init。
     */
    private GenericService getOrCreateGeneric(DubboConfig cfg) {
        String cacheKey = cfg.getInterfaceName() + "#"
                + (cfg.getGroup() == null ? "" : cfg.getGroup()) + "#"
                + (cfg.getVersion() == null ? "" : cfg.getVersion());

        return referenceCache.computeIfAbsent(cacheKey, k -> {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setInterface(cfg.getInterfaceName());  // 关键:String,无需 Class.forName
            reference.setGeneric(true);                      // 关键:开启泛化调用
            reference.setGroup(cfg.getGroup());
            reference.setVersion(cfg.getVersion());
            reference.setTimeout(cfg.getTimeoutMs() == null ? 5000 : cfg.getTimeoutMs());
            reference.setRetries(cfg.getRetries() == null ? 0 : cfg.getRetries());
            reference.setCheck(false);                       // 启动时不强制要求 Provider 在线
            reference.setLazy(true);                        // 懒加载,首次调用时再连接 Nacos
            // 可选直连旁路:当 dubbo.test.direct.url 这个 JVM system property 被显式声明时,
            // 不走 Nacos 服务发现,直接连目标 IP:PORT,用于测试/排错场景。
            // 不主动 setProperty(避免误改系统属性),只在确实声明了才使用。
            String directUrl = System.getProperty("dubbo.test.direct.url");
            log.info("[DubboInvoker] directUrl: {}", directUrl);
            if (directUrl != null && !directUrl.isBlank()) {
                reference.setUrl(directUrl);
                log.info("[DubboInvoker] use DIRECT url (bypass Nacos): {}", directUrl);
            }
            log.info("[DubboInvoker] create GenericService ReferenceConfig: {}", cacheKey);
            return reference;
        }).get();
    }

    /**
     * 在 DubboInvoker 加载时(早于 ReferenceConfig init)强制把 Consumer 切到
     * 接口级服务发现(Interface-First)。这是修复 Dubbo 3.x "No provider available"
     * 的关键:默认 Application-First 模式下,Consumer 需要 Provider 把 metadata
     * 注册到 Nacos 才能拿到 invoker,GenericService + 普通 yml 配置下极易踩坑。
     * 切回 Interface-First(2.x 行为)最稳定。
     */
    private static void forceInterfaceFirstServiceDiscovery() {
        String key = "dubbo.application.service-discovery-migration.force";
        if (!"INTERFACE_FIRST".equals(System.getProperty(key))) {
            System.setProperty(key, "INTERFACE_FIRST");
            log.info("[DubboInvoker] force service-discovery-migration=INTERFACE_FIRST");
        }
    }
    static {
        forceInterfaceFirstServiceDiscovery();
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
                log.info("[DubboInvoker] unwrap root wrapper [{}] for type {}", root.getFieldName(), parameterType);
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
     * 容器销毁时关闭所有 ReferenceConfig,释放连接。
     */
    @PreDestroy
    public void destroy() {
        log.info("[DubboInvoker] destroying {} cached references", referenceCache.size());
        for (ReferenceConfig<GenericService> reference : referenceCache.values()) {
            try {
                reference.destroy();
            } catch (Exception e) {
                log.warn("[DubboInvoker] destroy reference failed: {}", e.getMessage());
            }
        }
        referenceCache.clear();
    }
}
