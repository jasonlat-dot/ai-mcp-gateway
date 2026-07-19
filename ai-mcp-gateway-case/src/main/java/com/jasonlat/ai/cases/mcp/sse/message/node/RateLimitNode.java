package com.jasonlat.ai.cases.mcp.sse.message.node;

import com.jasonlat.ai.cases.mcp.sse.message.AbstractMcpSseMessageSupport;
import com.jasonlat.ai.cases.mcp.sse.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.domain.auth.model.entity.RateLimitCommandEntity;
import com.jasonlat.ai.domain.auth.service.IAuthRateLimitService;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.enums.SessionMessageHandlerMethodEnum;
import com.jasonlat.ai.types.enums.McpErrorCodes;
import com.jasonlat.ai.types.exception.AppException;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

/**
 * @author jasonlat
 * 2026-07-17  01:25
 */
@Slf4j
@Service("rateLimitNode")
public class RateLimitNode extends AbstractMcpSseMessageSupport {

    @Resource(name = "mcpMessageMessageHandleNode")
    private SseMessageHandleNode messageHandleNode;

    @Resource
    private IAuthRateLimitService rateLimitService;

    /**
     * 业务流程处理方法
     * <p>
     * 子类需要实现此方法来定义具体的业务处理逻辑。
     * 该方法在异步数据加载完成后执行。
     * </p>
     *
     * @param requestParameter 请求参数
     * @param dynamicContext   动态上下文
     * @return 处理结果
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected ResponseEntity<Object> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        try {
            // 判断命中工具调用做限流处理
            if (requestParameter.getJsonRpcMessage() instanceof McpSchemaVO.JsonRpcRequest request) {
                String method = request.method();
                log.info("mcp method: {}", method);
                // 是（true）否（false）命中限流
                SessionMessageHandlerMethodEnum sessionMessageHandlerMethodEnum = SessionMessageHandlerMethodEnum.getByMethod(method);
                if (SessionMessageHandlerMethodEnum.TOOLS_CALL.equals(sessionMessageHandlerMethodEnum)) {
                    boolean isHit = rateLimitService.rateLimit(new RateLimitCommandEntity(requestParameter.getGatewayId(), requestParameter.getApiKey(), method));
                    if (isHit) {
                        log.warn("消息处理 mcp message RootNode - 命中限流{} {}", requestParameter.getGatewayId(), requestParameter.getApiKey());
                        SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
                        Sinks.Many<ServerSentEvent<String>> sink = sessionConfigVO.getSink();
                        // 立刻手动结束Flux流，触发doFinally，会话直接删除
                        sink.tryEmitError(new AppException(McpErrorCodes.INSUFFICIENT_PERMISSIONS, "fail to auth apikey rateLimiter"));

                        return null;
                    }
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
        return messageHandleNode;
    }
}
