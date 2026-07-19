package com.jasonlat.ai.cases.mcp.sse.session;

import com.jasonlat.ai.cases.mcp.IMcpSessionService;
import com.jasonlat.ai.cases.mcp.sse.session.factory.DefaultMcpSessionFactory;
import com.jasonlat.ai.types.exception.AppException;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static com.jasonlat.ai.types.enums.ResponseCode.METHOD_NOT_FOUND;

/**
 * @author jasonlat
 * 2026-04-22  20:23
 */
@Slf4j
@Service
public class McpSseSessionService implements IMcpSessionService {

    /**
     * 获取 MCP 会话服务
     *
     * @param gatewayId 网关ID
     * @param apiKey apiKey
     * @param sessionId 会话ID
     * @return 流式响应
     */
    @Override
    public Flux<ServerSentEvent<String>> getMcpSession(String gatewayId, String apiKey, String sessionId) throws Exception {
        throw new AppException(METHOD_NOT_FOUND.getCode(), METHOD_NOT_FOUND.getInfo());
    }

    @Resource
    private DefaultMcpSessionFactory defaultMcpSessionFactory;

    @Override
    public Flux<ServerSentEvent<String>> createMcpSession(String gatewayId, String apiKey) throws Exception {
        StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> strategyHandler = defaultMcpSessionFactory.strategyHandler();

        DefaultMcpSessionFactory.DynamicContext dynamicContext = new DefaultMcpSessionFactory.DynamicContext();
        dynamicContext.setValue("apiKey", apiKey);

        return strategyHandler.apply(gatewayId, dynamicContext);
    }
}
