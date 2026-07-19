package com.jasonlat.ai.cases.mcp;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface IMcpSessionService {
    /**
     * 创建SSE会话连接
     * @param gatewayId 网关ID
     * @param apiKey apiKey
     */
    Flux<ServerSentEvent<String>> createMcpSession(String gatewayId, String apiKey) throws Exception;

    /**
     * 获取 MCP 会话服务
     *
     * @param sessionId 会话ID
     * @return 流式响应
     */
    Flux<ServerSentEvent<String>> getMcpSession(String gatewayId, String apiKey, String sessionId) throws Exception;

}
