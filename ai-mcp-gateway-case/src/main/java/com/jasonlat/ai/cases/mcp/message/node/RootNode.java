package com.jasonlat.ai.cases.mcp.message.node;

import com.jasonlat.ai.cases.mcp.message.AbstractMcpMessageSupport;
import com.jasonlat.ai.cases.mcp.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import com.jasonlat.ai.domain.auth.model.entity.RateLimitCommandEntity;
import com.jasonlat.ai.domain.auth.service.IAuthRateLimitService;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.enums.SessionMessageHandlerMethodEnum;
import com.jasonlat.ai.types.enums.McpErrorCodes;
import com.jasonlat.ai.types.exception.AppException;
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
    protected ResponseEntity<Object> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return sessionNode;
    }
}
