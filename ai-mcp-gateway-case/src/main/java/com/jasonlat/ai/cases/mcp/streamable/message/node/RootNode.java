package com.jasonlat.ai.cases.mcp.streamable.message.node;


import com.jasonlat.ai.cases.mcp.streamable.message.AbstractMcpStreamableMessageServiceSupport;
import com.jasonlat.ai.cases.mcp.streamable.message.factory.DefaultMcpStreamableMessageFactory;
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
import org.springframework.stereotype.Service;

/**
 * 根节点
 */
@Slf4j
@Service("mcpStreamableMessageRootNode")
public class RootNode extends AbstractMcpStreamableMessageServiceSupport {

    @Resource(name = "mcpStreamableMessageSessionNode")
    private SessionNode sessionNode;

    @Resource(name = "mcpStreamableMessageInitializeNode")
    private InitializeNode initializeNode;

    @Resource
    private IAuthRateLimitService authRateLimitService;

    @Override
    protected ResponseEntity<?> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpStreamableMessageFactory.DynamicContext dynamicContext) throws Exception {
        try {
            log.info("Streamable 消息处理 RootNode:{}", requestParameter);

            if (requestParameter.getJsonRpcMessage() instanceof McpSchemaVO.JsonRpcRequest request) {
                String method = request.method();
                SessionMessageHandlerMethodEnum sessionMessageHandlerMethodEnum = SessionMessageHandlerMethodEnum.getByMethod(method);
                if (SessionMessageHandlerMethodEnum.TOOLS_CALL.equals(sessionMessageHandlerMethodEnum)) {
                    boolean isHit = authRateLimitService.rateLimit(new RateLimitCommandEntity(requestParameter.getGatewayId(), requestParameter.getApiKey()));
                    if (isHit) {
                        log.warn("Streamable 消息处理 RootNode - 命中限流{} {}", requestParameter.getGatewayId(), requestParameter.getApiKey());
                        throw new AppException(McpErrorCodes.INSUFFICIENT_PERMISSIONS, "fail to auth apikey rateLimiter");
                    }
                }
            }

            return router(requestParameter, dynamicContext);
        } catch (Exception e) {
            log.error("Streamable 消息处理 RootNode:{}", requestParameter, e);
            throw e;
        }
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpStreamableMessageFactory.DynamicContext, ResponseEntity<?>> get(HandleMessageCommandEntity requestParameter, DefaultMcpStreamableMessageFactory.DynamicContext dynamicContext) throws Exception {
        if (requestParameter.getJsonRpcMessage() instanceof McpSchemaVO.JsonRpcRequest request
                && SessionMessageHandlerMethodEnum.INITIALIZE.getMethod().equals(request.method())) {
            return initializeNode;
        }
        return sessionNode;
    }

}
