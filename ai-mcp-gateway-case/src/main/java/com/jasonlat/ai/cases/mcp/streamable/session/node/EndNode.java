package com.jasonlat.ai.cases.mcp.streamable.session.node;


import com.jasonlat.ai.cases.mcp.streamable.session.AbstractMcpStreamableSessionSupport;
import com.jasonlat.ai.cases.mcp.streamable.session.factory.DefaultMcpStreamableSessionFactory;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * 结束节点
 */
@Slf4j
@Service("mcpStreamableSessionEndNode")
public class EndNode extends AbstractMcpStreamableSessionSupport {
    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpStreamableSessionFactory.DynamicContext dynamicContext) throws Exception {
        log.info("获取 Streamable 会话-EndNode gatewayId:{} sessionId:{}", dynamicContext.getGatewayId(), requestParameter);

        SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
        String sessionId = sessionConfigVO.getSessionId();

        return sessionConfigVO.getSink().asFlux()
                .mergeWith(Flux.interval(Duration.ofSeconds(60))
                        .map(i -> ServerSentEvent.<String>builder()
                                .event("ping")
                                .data("ping")
                                .build()))
                .doFinally(signalType -> {
                    log.info(
                            "Streamable SSE 监听结束 sessionId:{} signal:{}",
                            sessionId,
                            signalType
                    );
                    sessionManagementService.removeSession(sessionId);
                });
    }

    @Override
    public StrategyHandler<String, DefaultMcpStreamableSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpStreamableSessionFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
