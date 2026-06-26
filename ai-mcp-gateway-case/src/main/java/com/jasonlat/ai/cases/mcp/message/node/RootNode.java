package com.jasonlat.ai.cases.mcp.message.node;

import com.jasonlat.ai.cases.mcp.message.AbstractMcpMessageSupport;
import com.jasonlat.ai.cases.mcp.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author jasonlat
 * 2026-06-26  19:24
 */
@Slf4j
@Service("mcpMessageRootNode")
public class RootNode extends AbstractMcpMessageSupport {

    @Resource(name = "mcpMessageSessionNode")
    private SessionNode sessionNode;

    @Override
    protected ResponseEntity<Void> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        try {
            log.info("RootNode handle mcp message: {}", requestParameter.toString());

            return router(requestParameter, dynamicContext);
        } catch (Exception e) {
            log.error("RootNode handle mcp message error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Void>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return sessionNode;
    }
}
