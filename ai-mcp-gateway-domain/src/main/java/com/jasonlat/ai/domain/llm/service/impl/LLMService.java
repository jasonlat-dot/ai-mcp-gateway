package com.jasonlat.ai.domain.llm.service.impl;

import com.jasonlat.ai.domain.llm.model.entity.BuildChatModelCommandEntity;
import com.jasonlat.ai.domain.llm.model.valobj.McpConfigVO;
import com.jasonlat.ai.domain.llm.service.ILLMService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

/**
 * 管理端网关联调使用的大模型服务。
 * 每次调用创建独立的 MCP 客户端，请求完成后立即关闭，不做任何缓存。
 */
@Slf4j
@Service
public class LLMService implements ILLMService {

    @Resource
    private OpenAiApi openAiApi;

    @Value("${openai.client.model:gpt-5.6}")
    private String model;

    @Override
    public String callGateway(BuildChatModelCommandEntity commandEntity, String message) {
        McpConfigVO config = commandEntity.getMcpConfigVO();
        log.info("创建临时 MCP 对话模型 gatewayId:{} sseEndpoint:{} timeout:{} authConfigured:{}",
                commandEntity.getGatewayId(), config.getSseEndpoint(), config.getTimeout(),
                StringUtils.isNotBlank(config.getAuthApiKey()));
        UriComponentsBuilder sseEndpointBuilder = UriComponentsBuilder.fromPath(config.getSseEndpoint());
        if (StringUtils.isNotBlank(config.getAuthApiKey())) {
            sseEndpointBuilder.queryParam("api_key", config.getAuthApiKey());
        }
        String sseEndpoint = sseEndpointBuilder.build().encode().toUriString();

        HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport
                .builder(config.getBaseUri())
                .sseEndpoint(sseEndpoint)
                .build();

        McpSyncClient mcpSyncClient = McpClient
                .sync(sseClientTransport)
                .requestTimeout(Duration.ofMillis(config.getTimeout())).build();

        var mcpInitializeResult = mcpSyncClient.initialize();
        log.info("MCP 初始化结果 gatewayId:{}", mcpInitializeResult);

        ToolCallback[] callbacks = new SyncMcpToolCallbackProvider(mcpSyncClient).getToolCallbacks();

        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .toolCallbacks(callbacks)
                        .build())
                .build();

        return chatModel.call(message);
    }

}
