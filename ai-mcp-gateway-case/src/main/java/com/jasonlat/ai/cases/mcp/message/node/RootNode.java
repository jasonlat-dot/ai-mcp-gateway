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

    @Resource
    private IAuthRateLimitService rateLimitService;

    @Override
    protected ResponseEntity<Object> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        try {
            // 判断命中工具调用做限流处理
            if (requestParameter.getJsonRpcMessage() instanceof McpSchemaVO.JsonRpcRequest request) {
                String method = request.method();
                log.info("mcp method: {}", method);
                // 是（true）否（false）命中限流
                boolean isHit = rateLimitService.rateLimit(new RateLimitCommandEntity(requestParameter.getGatewayId(), requestParameter.getApiKey(), method));
                if (isHit) {
                    log.warn("消息处理 mcp message RootNode - 命中限流{} {}", requestParameter.getGatewayId(), requestParameter.getApiKey());
                    throw new AppException(McpErrorCodes.INSUFFICIENT_PERMISSIONS, "fail to auth apikey rateLimiter");
                }
            }
            return router(requestParameter, dynamicContext);
        } catch (Exception e) {
            log.error("RootNode handle mcp message error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return sessionNode;
    }
}
