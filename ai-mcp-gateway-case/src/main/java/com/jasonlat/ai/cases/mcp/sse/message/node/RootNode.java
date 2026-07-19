package com.jasonlat.ai.cases.mcp.sse.message.node;

import com.jasonlat.ai.cases.mcp.sse.message.AbstractMcpSseMessageSupport;
import com.jasonlat.ai.cases.mcp.sse.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author jasonlat
 * 2026-06-26  19:24
 */
@Slf4j
@Service("mcpMessageRootNode")
public class RootNode extends AbstractMcpSseMessageSupport {

    @Resource(name = "mcpMessageSessionNode")
    private SessionNode sessionNode;


    @Override
    protected ResponseEntity<Object> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return sessionNode;
    }
}
