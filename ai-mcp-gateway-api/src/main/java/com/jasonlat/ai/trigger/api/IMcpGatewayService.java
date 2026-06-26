package com.jasonlat.ai.trigger.api;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IMcpGatewayService {

    /**
     * 建立SSE连接
     * @param gatewayId 网关ID
     */
    Flux<ServerSentEvent<String>> establishSseConnection(String gatewayId);

    Mono<ResponseEntity<Void>> handleMessage(String gatewayId, String sessionId, String messageBody);
}
