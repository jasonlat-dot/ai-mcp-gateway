package com.jasonlat.ai.trigger.api;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface IMcpGatewayService {

    /**
     * 建立SSE连接
     * @param gatewayId 网关ID
     */
    Flux<ServerSentEvent<String>> establishSseConnection(String gatewayId);
}
