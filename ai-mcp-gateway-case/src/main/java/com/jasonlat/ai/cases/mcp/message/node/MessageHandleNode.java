package com.jasonlat.ai.cases.mcp.message.node;

import com.jasonlat.ai.cases.mcp.message.AbstractMcpMessageSupport;
import com.jasonlat.ai.cases.mcp.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author jasonlat
 * 2026-06-26  19:38
 */
@Slf4j
@Service("mcpMessageMessageHandleNode")
public class MessageHandleNode extends AbstractMcpMessageSupport {

    private static final long EMIT_RETRY_TIMEOUT_NANOS = Duration.ofMillis(200).toNanos();
    private static final long EMIT_RETRY_PAUSE_NANOS = TimeUnit.MICROSECONDS.toNanos(100);

    @Override
    protected ResponseEntity<Object> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        log.info("MessageHandleNode handle mcp message: {}", requestParameter.toString());

        McpSchemaVO.JsonRpcResponse jsonRpcResponse = sessionMessageService.processHandleMessage(requestParameter);
        if (null != jsonRpcResponse) {
            String responseJson = objectMapper.writeValueAsString(jsonRpcResponse);

            SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
            ServerSentEvent<String> responseEvent = ServerSentEvent.<String>builder()
                    .event("message")
                    .data(responseJson)
                    .build();
            Sinks.EmitResult emitResult = emitWithRetry(sessionConfigVO, responseEvent);

            if (emitResult.isFailure()) {
                log.warn("发送 MCP SSE 响应失败 sessionId:{} result:{}",
                        sessionConfigVO.getSessionId(), emitResult);
                HttpStatus status = emitResult == Sinks.EmitResult.FAIL_OVERFLOW
                        ? HttpStatus.SERVICE_UNAVAILABLE
                        : HttpStatus.GONE;
                return ResponseEntity.status(status).build();
            }
        }
        return ResponseEntity.accepted().build();
    }

    private Sinks.EmitResult emitWithRetry(
            SessionConfigVO sessionConfigVO,
            ServerSentEvent<String> responseEvent) {
        long deadline = System.nanoTime() + EMIT_RETRY_TIMEOUT_NANOS;
        Sinks.EmitResult emitResult;
        do {
            emitResult = sessionConfigVO.getSink().tryEmitNext(responseEvent);
            if (emitResult != Sinks.EmitResult.FAIL_NON_SERIALIZED) return emitResult;
            LockSupport.parkNanos(EMIT_RETRY_PAUSE_NANOS);
        } while (System.nanoTime() < deadline);
        return emitResult;
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return super.get(requestParameter, dynamicContext);
    }
}
