package com.jasonlat.ai.cases.mcp.streamable.session;

import com.jasonlat.ai.cases.mcp.IMcpSessionService;
import com.jasonlat.ai.cases.mcp.streamable.session.factory.DefaultMcpStreamableSessionFactory;
import com.jasonlat.ai.domain.session.service.ISessionManagementService;
import com.jasonlat.ai.types.exception.AppException;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
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

    @Resource
    private DefaultMcpStreamableSessionFactory defaultMcpStreamableSessionFactory;

    @Resource
    private ISessionManagementService sessionManagementService;

    /**
     * Streamable HTTP 会话由 POST initialize 创建，GET 只负责监听已有会话。
     */
    @Override
    public Flux<ServerSentEvent<String>> createMcpSession(String gatewayId, String apiKey) {
        throw new AppException(METHOD_NOT_FOUND.getCode(), METHOD_NOT_FOUND.getInfo());
    }

    @Override
    public Flux<ServerSentEvent<String>> getMcpSession(String gatewayId, String apiKey, String sessionId) throws Exception {
        StrategyHandler<String, DefaultMcpStreamableSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> strategyHandler =
                defaultMcpStreamableSessionFactory.strategyHandler();

        DefaultMcpStreamableSessionFactory.DynamicContext dynamicContext = new DefaultMcpStreamableSessionFactory.DynamicContext();
        dynamicContext.setGatewayId(gatewayId);
        dynamicContext.setApiKey(apiKey);

        return strategyHandler.apply(sessionId, dynamicContext);
    }

    public void deleteMcpSession(String sessionId) {
        sessionManagementService.removeSession(sessionId);
    }

}
