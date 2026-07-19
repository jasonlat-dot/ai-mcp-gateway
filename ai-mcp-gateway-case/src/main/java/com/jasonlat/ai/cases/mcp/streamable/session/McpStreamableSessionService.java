package com.jasonlat.ai.cases.mcp.streamable.session;

import com.jasonlat.ai.cases.mcp.IMcpSessionService;
import com.jasonlat.ai.types.exception.AppException;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static com.jasonlat.ai.types.enums.ResponseCode.METHOD_NOT_FOUND;

/**
 * @author jasonlat
 * 2026-07-19  15:04
 */
@Service
public class McpStreamableSessionService implements IMcpSessionService {

    @Override
    public Flux<ServerSentEvent<String>> createMcpSession(String gatewayId, String apiKey) throws Exception {
        throw new AppException(METHOD_NOT_FOUND.getCode(), METHOD_NOT_FOUND.getInfo());
    }

    @Override
    public Flux<ServerSentEvent<String>> getMcpSession(String gatewayId, String apiKey, String sessionId) throws Exception {

        return null;
    }

}
