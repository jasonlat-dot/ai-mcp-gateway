package com.jasonlat.ai.trigger.api;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IMcpGatewayService {

    /**
     * 建立SSE连接
     * @param gatewayId 网关ID
     * @param apiKey apiKey
     *
     */
    Flux<ServerSentEvent<String>> establishSseConnection(String gatewayId, String apiKey) throws Exception;

    Mono<ResponseEntity<?>> handleMessage(String gatewayId, String apiKey, String sessionId, String messageBody) throws Exception;
}
