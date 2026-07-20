package com.jasonlat.ai.domain.llm.service.impl;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.llm.model.entity.BuildChatModelCommandEntity;
import com.jasonlat.ai.domain.llm.model.valobj.McpConfigVO;
import com.jasonlat.ai.domain.llm.model.valobj.McpTypeEnumVO;
import com.jasonlat.ai.domain.llm.service.ILLMService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理端网关联调使用的大模型服务。
 * 每次调用创建独立的 MCP 客户端，请求完成后立即关闭，不做任何缓存。
 */
@Slf4j
@Service
public class LLMService implements ILLMService {

    // 全局策略映射（构造函数方式，只初始化一次）
    private final Map<McpTypeEnumVO, ToolCallbackBuilderStrategy> strategyMap = new HashMap<>(2);

    @Resource
    private OpenAiApi openAiApi;

    @Value("${openai.client.model:gpt-5.6}")
    private String model;

    // 构造函数，初始化策略映射
    public LLMService() {
        strategyMap.put(McpTypeEnumVO.SSE, new SseToolCallbackBuilderStrategy());
        strategyMap.put(McpTypeEnumVO.STREAMABLE, new StreamableToolCallbackBuilderStrategy());
    }

    @Override
    public String callGateway(BuildChatModelCommandEntity commandEntity, String message) {
        log.info("构建对话模型 gatewayId:{} mcp:{} type:{}", commandEntity.getGatewayId(), JSON.toJSONString(commandEntity.getMcpConfigVO()), commandEntity.getMcpType());

        // mcp 配置
        McpConfigVO mcpConfigVO = commandEntity.getMcpConfigVO();

        // model 配置 + mcp 服务
        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .toolCallbacks(buildToolCallback(mcpConfigVO, commandEntity.getMcpType()))
                        .build())
                .build();

        return chatModel.call(message);
    }

    /**
     * 工具策略接口（直接在内部类实现）
     */
    private interface ToolCallbackBuilderStrategy {
        ToolCallback[] build(McpConfigVO config);
    }

    /**
     * SSE 策略实现
     */
    private static class SseToolCallbackBuilderStrategy implements ToolCallbackBuilderStrategy {
        @Override
        public ToolCallback[] build(McpConfigVO mcpConfigVO) {
            String sseEndPoint = mcpConfigVO.getEndpoint();
            if (StringUtils.isNotBlank(mcpConfigVO.getAuthApiKey())) {
                sseEndPoint += "?api_key=" + mcpConfigVO.getAuthApiKey();
            }
            HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport
                    .builder(mcpConfigVO.getBaseUri())
                    .sseEndpoint(sseEndPoint)
                    .build();
            McpSyncClient mcpSyncClient = McpClient
                    .sync(sseClientTransport)
                    .requestTimeout(Duration.ofMillis(mcpConfigVO.getTimeout())).build();
            var initialize = mcpSyncClient.initialize();
            log.info("tool sse mcp initialize {}", initialize);
            return SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClient).build().getToolCallbacks();
        }

    }

    /**
     * Streamable 策略实现
     */
    private static class StreamableToolCallbackBuilderStrategy implements ToolCallbackBuilderStrategy {
        @Override
        public ToolCallback[] build(McpConfigVO mcpConfigVO) {
            String endpoint = mcpConfigVO.getEndpoint();
            if (StringUtils.isNotBlank(mcpConfigVO.getAuthApiKey())) {
                endpoint += "?api_key=" + mcpConfigVO.getAuthApiKey();
            }
            McpClientTransport mcpClientTransport = HttpClientStreamableHttpTransport
                    .builder(mcpConfigVO.getBaseUri())
                    .endpoint(endpoint)
                    .build();
            McpSyncClient mcpSyncClient = McpClient.sync(mcpClientTransport)
                    .requestTimeout(Duration.ofMillis(mcpConfigVO.getTimeout())).build();
            var init_streamable = mcpSyncClient.initialize();
            log.info("tool streamable mcp initialize {}", init_streamable);
            return SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClient).build().getToolCallbacks();
        }
    }

    /**
     * 工具回调分派策略，根据类型选择实现。
     * mcpType 为 null 或不支持时，默认为 SSE。
     */
    public ToolCallback[] buildToolCallback(McpConfigVO mcpConfigVO, McpTypeEnumVO mcpType) {
        ToolCallbackBuilderStrategy strategy = strategyMap.get(mcpType);
        if (strategy == null) {
            // 默认 SSE
            strategy = strategyMap.get(McpTypeEnumVO.SSE);
        }
        return strategy.build(mcpConfigVO);
    }

}
