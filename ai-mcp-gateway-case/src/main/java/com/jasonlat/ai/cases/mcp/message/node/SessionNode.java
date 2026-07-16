package com.jasonlat.ai.cases.mcp.message.node;

import com.jasonlat.ai.cases.mcp.message.AbstractMcpMessageSupport;
import com.jasonlat.ai.cases.mcp.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author jasonlat
 * 2026-06-26  19:38
 */
@Slf4j
@Service("mcpMessageSessionNode")
public class SessionNode extends AbstractMcpMessageSupport {

    @Resource(name = "rateLimitNode")
    private RateLimitNode rateLimitNode;

    @Override
    protected ResponseEntity<Object> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        log.info("SessionNode handle mcp message: {}", requestParameter.toString());
        SessionConfigVO sessionConfigVO = sessionManagementService.getSession(requestParameter.getSessionId());
        if (sessionConfigVO == null) {
            log.warn("会话不存在 gatewayId:{} sessionId:{}", requestParameter.getGatewayId(), requestParameter.getSessionId());
            return ResponseEntity.notFound().build();
        }
        // 设置会话信息
        dynamicContext.setSessionConfigVO(sessionConfigVO);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return rateLimitNode;
    }
}
