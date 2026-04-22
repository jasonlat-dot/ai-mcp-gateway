package com.jasonlat.ai.cases.mcp.session;

import com.jasonlat.ai.cases.mcp.IMcpSessionService;
import com.jasonlat.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author jasonlat
 * 2026-04-22  20:23
 */
@Slf4j
@Service
public class McpSessionService implements IMcpSessionService {

    @Resource
    private DefaultMcpSessionFactory defaultMcpSessionFactory;

    /**
     * 创建SSE会话连接
     *
     * @param gatewayId 网关ID
     */
    @Override
    public Flux<ServerSentEvent<String>> establishSseConnection(String gatewayId) throws Exception {
        StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> strategyHandler = defaultMcpSessionFactory.strategyHandler();
        return strategyHandler.apply(gatewayId, new DefaultMcpSessionFactory.DynamicContext());
    }
}
