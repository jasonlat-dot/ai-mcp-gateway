package com.jasonlat.ai.cases.mcp;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface IMcpSessionService {
    /**
     * 创建SSE会话连接
     * @param gatewayId 网关ID
     */
    Flux<ServerSentEvent<String>> establishSseConnection(String gatewayId) throws Exception;
}
