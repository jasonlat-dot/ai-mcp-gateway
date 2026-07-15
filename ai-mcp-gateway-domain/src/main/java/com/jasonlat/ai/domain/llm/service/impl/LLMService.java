package com.jasonlat.ai.domain.llm.service.impl;

import com.alibaba.fastjson.JSON;
import com.jasonlat.ai.domain.llm.model.entity.BuildChatModelCommandEntity;
import com.jasonlat.ai.domain.llm.model.valobj.McpConfigVO;
import com.jasonlat.ai.domain.llm.service.ILLMService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 大模型服务
 */
@Slf4j
@Service
public class LLMService implements ILLMService {

    private final Map<String, ChatModel> chatModelMap = new ConcurrentHashMap<>();

    @Resource
    private OpenAiApi openAiApi;

    @Value("${openai.client.model:gpt-5.6}")
    private String model;

    @Override
    public void buildChatModel(BuildChatModelCommandEntity commandEntity) {
        log.info("构建对话模型 gatewayId:{} mcp:{}", commandEntity.getGatewayId(), JSON.toJSONString(commandEntity.getMcpConfigVO()));

        // mcp 配置
        McpConfigVO mcpConfigVO = commandEntity.getMcpConfigVO();

        // model 配置 + mcp 服务
        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .toolCallbacks(buildToolCallback(mcpConfigVO))
                        .build())
                .build();

        // 写入缓存
        chatModelMap.put(commandEntity.getGatewayId(), chatModel);
    }

    public ToolCallback[] buildToolCallback(McpConfigVO mcpConfigVO) {
        String sseEndPoint = mcpConfigVO.getSseEndpoint();
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

        return new SyncMcpToolCallbackProvider(mcpSyncClient).getToolCallbacks();
    }

    @Override
    public ChatModel getChatModel(String gatewayId) {
        return chatModelMap.get(gatewayId);
    }

}
