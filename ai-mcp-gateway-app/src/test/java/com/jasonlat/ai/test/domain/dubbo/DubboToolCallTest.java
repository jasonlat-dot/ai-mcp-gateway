package com.jasonlat.ai.test.domain.dubbo;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.enums.ProtocolType;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.domain.session.adapter.repository.ISessionRepository;
import com.jasonlat.ai.domain.session.service.message.handle.impl.ToolsCallHandler;
import com.jasonlat.ai.infrastructure.adapter.port.tool.DubboInvoker;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Dubbo RPC 全链路测试 — 验证 LLM 能通过 MCP Gateway 调通 dubbo-provider。
 *
 * <p>测试层次:
 * <ol>
 *   <li>底层测试:直接调 {@link DubboInvoker},验证 GenericService 链路正确</li>
 *   <li>全链路测试:通过 {@link ToolsCallHandler} → Repository → DubboInvoker,
 *       模拟 LLM 发起 {@code tools/call} 的完整流程</li>
 * </ol>
 *
 * <p>前置条件:
 * <ul>
 *   <li>Nacos(192.168.3.16:8848) 已启动</li>
 *   <li>ai-mcp-gateway-dubbo-provider 已启动并注册到 Nacos</li>
 *   <li>数据库已执行 {@code ai_mcp_gateway_v2_dubbo_data.sql} 注册工具和映射</li>
 * </ul>
 *
 * @author jasonlat
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class DubboToolCallTest {

    // ============================================================
    // 底层测试用 — 直接调 DubboInvoker,绕开数据库
    // ============================================================

    @Resource
    private DubboInvoker dubboInvoker;

//    @Resource
//    private ApplicationConfig applicationConfig;
//
//    @Resource
//    private RegistryConfig registryConfig;

    // ============================================================
    // 全链路测试用 — 通过 ToolsCallHandler + Repository
    // ============================================================

    @Resource
    private ToolsCallHandler toolsCallHandler;

    @Resource
    private ISessionRepository repository;

    // ============================================================
    // 测试数据
    // ============================================================

    /** 工具名 — 与 ai_mcp_gateway_v2_dubbo_data.sql 中注册的一致 */
    private static final String TOOL_NAME = "JavaSDKMCPClient_getCompanyEmployeeByDubbo";
    private static final String GATEWAY_ID = "gateway_001";

    @Before
    public void before() {
        // 在 Spring 加载 Dubbo 配置前,强制把 Consumer 切到 INTERFACE_FIRST。
        // System.setProperty 在 JVM 全局生效,Dubbo 启动时会读取。
        // 这一步必须放在 @Before,确保在 Dubbo ReferenceConfig 初始化前生效。
        System.setProperty("dubbo.application.service-discovery-migration.force", "APPLICATION_FIRST");

        log.info("========== DubboToolCallTest 前置检查 ==========");
//        log.info("Nacos registry: {}", registryConfig.getAddress());
//        log.info("Dubbo application: {}", applicationConfig.getName());
        log.info("force INTERFACE_FIRST: {}", System.getProperty("dubbo.application.service-discovery-migration.force"));
        log.info("tool_name={}, gateway_id={}", TOOL_NAME, GATEWAY_ID);
        log.info("请确认以下服务已就绪:");
        log.info("  1. Nacos(192.168.3.16:8848) 已启动");
        log.info("  2. dubbo-provider 已启动并注册到 Nacos");
        log.info("  3. 数据库已执行 ai_mcp_gateway_v2_dubbo_data.sql");
    }

    // =============================================================
    // 底层测试 — 直接用 DubboInvoker.invoke()
    // 优点:不依赖数据库配置,纯粹验证 GenericService 能否调通 Provider
    // =============================================================

    /**
     * 【底层测试 1】直接调 DubboInvoker,手动构造 DubboConfig,验证 RPC 能通。
     * <p>
     * 场景:用 LLM 传来的 MCP 结构参数(带 xxxRequest01 wrapper)调用,
     * 验证 wrapper 解包逻辑正确。
     */
    @Test
    public void test_dubboInvoker_directCall() {
        // 1. 构造 DubboConfig(模拟数据库读出来的配置)
        McpToolProtocolConfigVO.DubboConfig dubboConfig = McpToolProtocolConfigVO.DubboConfig.builder()
                .interfaceName("com.jasonlat.ai.dubbo.api.EmployeeService")
                .group("default")
                .version("1.0.0")
                .methodName("getCompanyEmployee")
                .parameterTypes(List.of("com.jasonlat.ai.dubbo.api.dto.EmployeeRequest"))
                .timeoutMs(5000)
                .retries(0)
                .build();

        // 构造 protocolConfig(把 DubboConfig 包进去,模拟 repository 返回值)
        // 注:DubboInvoker.invoke 只需要 dubboConfig 和 requestProtocolMappings,不需要 protocolId/toolName
        McpToolProtocolConfigVO protocolConfig = McpToolProtocolConfigVO.builder()
                .protocolType(ProtocolType.DUBBO)
                .dubboConfig(dubboConfig)
                .build();

        // 2. 构造 LLM 传入的参数(MCP 包装结构)
        Map<String, Object> arguments = new LinkedHashMap<>();
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("city", "beijing");
        Map<String, Object> company = new LinkedHashMap<>();
        company.put("name", "jd");
        company.put("type", "互联网");
        request.put("company", company);
        arguments.put("xxxRequest01", request);

        log.info("【底层测试1】调用 DubboInvoker, arguments={}", JSON.toJSONString(arguments));

        // 3. 调用
        Object result = dubboInvoker.invoke(protocolConfig, arguments);

        log.info("【底层测试1】调用成功, result={}", result);
        assert result != null;
        assert result instanceof String;
        String json = (String) result;
        assert json.contains("employees");
        log.info("✅ 底层测试1通过: DubboInvoker GenericService 调用成功");
    }

    /**
     * 【底层测试 2】不带 wrapper 的扁平参数,验证不拆包也能通。
     */
    @Test
    public void test_dubboInvoker_flatParams() {
        McpToolProtocolConfigVO.DubboConfig dubboConfig = McpToolProtocolConfigVO.DubboConfig.builder()
                .interfaceName("com.jasonlat.ai.dubbo.api.EmployeeService")
                .group("default")
                .version("1.0.0")
                .methodName("getCompanyEmployee")
                .parameterTypes(List.of("com.jasonlat.ai.dubbo.api.dto.EmployeeRequest"))
                .timeoutMs(5000)
                .retries(0)
                .build();

        McpToolProtocolConfigVO protocolConfig = McpToolProtocolConfigVO.builder()
                .protocolType(ProtocolType.DUBBO)
                .dubboConfig(dubboConfig)
                .build();

        // 扁平参数:直接是 POJO 结构,没有 xxxRequest01 wrapper
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("city", "shanghai");
        Map<String, Object> company = new LinkedHashMap<>();
        company.put("name", "alibaba");
        company.put("type", "电商");
        arguments.put("company", company);

        log.info("【底层测试2】调用 DubboInvoker(扁平参数), arguments={}", JSON.toJSONString(arguments));

        Object result = dubboInvoker.invoke(protocolConfig, arguments);

        log.info("【底层测试2】调用成功, result={}", result);
        assert result != null;
        log.info("✅ 底层测试2通过: 扁平参数直接调用成功");
    }

    // =============================================================
    // 全链路测试 — 通过 ToolsCallHandler,模拟 LLM 发起的 tools/call
    // 完整流程: tools/call → Repository(查DB) → DubboInvoker → Provider RPC
    // =============================================================

    /**
     * 【全链路测试 1】通过 MCP tools/call 完整链路调 Dubbo RPC。
     * <p>
     * 模拟 LLM 发送:
     * <pre>
     * {"name":"JavaSDKMCPClient_getCompanyEmployeeByDubbo",
     *  "arguments":{"xxxRequest01":{"city":"beijing","company":{"name":"jd","type":"互联网"}}}}
     * </pre>
     * 验证最终拿到包含 employees 列表的 JSON。
     */
    @Test
    public void test_toolsCall_dubbo_fullChain() {
        // 1. 模拟 MCP tools/call 请求(JSON-RPC 2.0 格式)
        String jsonRpcRequest = """
                {
                    "name": "JavaSDKMCPClient_getCompanyEmployeeByDubbo",
                    "arguments": {
                        "xxxRequest01": {
                            "city": "beijing",
                            "company": {
                                "name": "jd",
                                "type": "互联网"
                            }
                        }
                    }
                }
                """;

        Map<String, Object> callParams = JSON.parseObject(jsonRpcRequest);

        McpSchemaVO.JsonRpcRequest request = new McpSchemaVO.JsonRpcRequest(
                "2.0",
                "tools/call",
                UUID.randomUUID().toString(),
                callParams
        );

        log.info("【全链路测试1】发送 tools/call 请求, params={}", JSON.toJSONString(callParams));

        // 2. 调用 ToolsCallHandler(这是 MCP Gateway 接收 LLM 请求的入口)
        McpSchemaVO.JsonRpcResponse response = toolsCallHandler.handleMessage(GATEWAY_ID, request);

        log.info("【全链路测试1】收到响应, response={}", JSON.toJSONString(response));

        // 3. 验证
        // JsonRpcResponse 是 record,成功时 error == null
        assert response.error() == null : "tools/call 应返回成功, error=" + JSON.toJSONString(response.error());
        assert response.result() != null : "result 不应为 null";

        // result.content[0].text 即为 Provider 返回的 JSON
        Map<String, Object> resultMap = (Map<String, Object>) response.result();
        List<?> content = (List<?>) resultMap.get("content");
        assert content != null && !content.isEmpty();

        Map<String, Object> textBlock = (Map<String, Object>) content.get(0);
        String text = (String) textBlock.get("text");

        log.info("【全链路测试1】Provider 返回的 text={}", text);
        assert text != null && text.contains("employees") : "返回文本应包含 employees 字段";

        log.info("✅ 全链路测试1通过: LLM → MCP Gateway → Dubbo RPC 全链路调通");
    }

    /**
     * 【全链路测试 2】浦东 city=shanghai, company=alibaba。
     */
    @Test
    public void test_toolsCall_dubbo_shanghai() {
        Map<String, Object> callParams = JSON.parseObject("""
                {
                    "name": "JavaSDKMCPClient_getCompanyEmployeeByDubbo",
                    "arguments": {
                        "xxxRequest01": {
                            "city": "shanghai",
                            "company": {
                                "name": "alibaba",
                                "type": "电商"
                            }
                        }
                    }
                }
                """);

        McpSchemaVO.JsonRpcRequest request = new McpSchemaVO.JsonRpcRequest(
                "2.0", "tools/call", UUID.randomUUID().toString(), callParams);

        log.info("【全链路测试2】shanghai/alibaba 调用");
        McpSchemaVO.JsonRpcResponse response = toolsCallHandler.handleMessage(GATEWAY_ID, request);

        log.info("【全链路测试2】响应:{}", JSON.toJSONString(response));
        assert response.error() == null : "shanghai/alibaba 调用应成功, error=" + JSON.toJSONString(response.error());

        Map<String, Object> resultMap = (Map<String, Object>) response.result();
        List<?> content = (List<?>) resultMap.get("content");
        Map<String, Object> textBlock = (Map<String, Object>) content.get(0);
        String text = (String) textBlock.get("text");

        assert text.contains("shanghai") || text.contains("alibaba");
        log.info("✅ 全链路测试2通过: shanghai/alibaba 场景成功");
    }

    /**
     * 【全链路测试 3】验证工具不存在时的错误处理。
     */
    @Test
    public void test_toolsCall_toolNotFound() {
        Map<String, Object> callParams = Map.of(
                "name", "NonExistentTool",
                "arguments", Map.of("key", "value")
        );

        McpSchemaVO.JsonRpcRequest request = new McpSchemaVO.JsonRpcRequest(
                "2.0", "tools/call", UUID.randomUUID().toString(), callParams);

        log.info("【全链路测试3】调用不存在的工具");
        McpSchemaVO.JsonRpcResponse response = toolsCallHandler.handleMessage(GATEWAY_ID, request);

        log.info("【全链路测试3】响应:{}", JSON.toJSONString(response));
        // 不存在的工具应该在 ToolsCallHandler 里返回错误
        assert response.error() != null || response.result() == null :
                "不存在的工具应返回错误响应";
        log.info("✅ 全链路测试3通过: 工具不存在时返回错误");
    }

    // =============================================================
    // 辅助调试方法 — 直接打印 DB 中加载的配置,排查配置问题
    // =============================================================

    /**
     * 打印数据库中加载的工具配置(调试用)。
     */
    @Test
    public void debug_printToolConfig() {
        McpToolProtocolConfigVO config = repository.queryMcpGatewayProtocolConfig(GATEWAY_ID, TOOL_NAME);
        if (config == null) {
            log.warn("❌ 数据库中未找到工具配置! gateway_id={} tool_name={}", GATEWAY_ID, TOOL_NAME);
            log.warn("请确认已执行 ai_mcp_gateway_v2_dubbo_data.sql");
            return;
        }
        log.info("✅ 找到工具配置:");
        log.info("  protocolType={}", config.getProtocolType());
        if (config.getDubboConfig() != null) {
            log.info("  dubboConfig.interfaceName={}", config.getDubboConfig().getInterfaceName());
            log.info("  dubboConfig.methodName={}", config.getDubboConfig().getMethodName());
            log.info("  dubboConfig.parameterTypes={}", config.getDubboConfig().getParameterTypes());
            log.info("  dubboConfig.group={}", config.getDubboConfig().getGroup());
            log.info("  dubboConfig.version={}", config.getDubboConfig().getVersion());
            log.info("  dubboConfig.timeoutMs={}", config.getDubboConfig().getTimeoutMs());
        } else {
            log.warn("  dubboConfig is null! 检查数据库 mcp_gateway_tool.protocol_type 是否为 DUBBO");
        }
        log.info("  requestMappings count={}",
                config.getRequestProtocolMappings() == null ? 0 : config.getRequestProtocolMappings().size());
    }
}
