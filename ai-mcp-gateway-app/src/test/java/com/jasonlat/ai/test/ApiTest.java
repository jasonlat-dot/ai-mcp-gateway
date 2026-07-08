package com.jasonlat.ai.test;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.config.properties.OpenAiClientProperties;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.time.Duration;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {


    @Resource
    private OpenAiChatModel.Builder chatModelBuilder;

    @Value("${baidu.api-key}")
    private String baiduApiKey;

    @Test
    public void test() {
        log.info("测试完成");
    }

    @Test
    public void test_mcp() throws InterruptedException {

        OpenAiChatModel chatModel = chatModelBuilder.defaultOptions(
                OpenAiChatOptions.builder()
                        .model("gpt-5.5")
                        .toolCallbacks(new SyncMcpToolCallbackProvider(sseMcpClient03()).getToolCallbacks())
                        .build()
                ).build();

        Prompt prompt = new Prompt(
                UserMessage.builder()
                        .text("""
                                获取公司雇员信息，信息如下：
                                城市：北京
                                公司：谷歌
                                雇员：jasonlat
                                """)
                        .build()
        );
        ChatResponse response = chatModel.call(prompt);
        System.out.println("测试结果：" + JSON.toJSONString(response));
    }

    // http://localhost:8888/api-gateway/gateway_001/mcp/sse?api_key=gw-SjnhpA5S6CI5bx9Vxvh8ATyezZzqclNNP2ikf18wSHHJMR1H
    public static McpSyncClient sseMcpClient03() {
        HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport
                .builder("http://localhost:8888")
                .sseEndpoint("/api-gateway/gateway_001/mcp/sse?api_key=gw-8mvSBT1k9ydEKw9ZN0OSLnp3qcSwj2mde8890FGKdrfNwFto")
                .build();

        McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport)
                .requestTimeout(Duration.ofMinutes(3600))
                .build();
        var init_sse = mcpSyncClient.initialize();
        log.info("Tool SSE MCP Initialized {}", init_sse);

        return mcpSyncClient;
    }

    public McpSyncClient sseMcpClient02() {
        log.info("sseMcpClient02......");
        HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport
                .builder("http://127.0.0.1:8888")
                .sseEndpoint("/api-gateway/test10001/mcp/sse")
                .build();

        McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMillis(36000)).build();
        McpSchema.InitializeResult initialize = mcpSyncClient.initialize();

        log.info("initialize: {}", initialize);
        return mcpSyncClient;
    }

    public McpSyncClient sseMcpClient() {
        log.info("sseMcpClient......");
        HttpClientSseClientTransport sse = HttpClientSseClientTransport
                .builder("http://appbuilder.baidu.com")
                .sseEndpoint("/v2/ai_search/mcp/sse?api_key=" + baiduApiKey)
                .build();

        McpSyncClient mcpSyncClient = McpClient.sync(sse).requestTimeout(Duration.ofMillis(36000)).build();
        McpSchema.InitializeResult initialize = mcpSyncClient.initialize();

        log.info("initialize: {}", initialize);
        return mcpSyncClient;
    }

}
