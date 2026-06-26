package com.jasonlat.ai.cases.mcp.message.node;

import com.jasonlat.ai.cases.mcp.message.AbstractMcpMessageSupport;
import com.jasonlat.ai.cases.mcp.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

/**
 * @author jasonlat
 * 2026-06-26  19:38
 */
@Slf4j
@Service("mcpMessageMessageHandleNode")
public class MessageHandleNode extends AbstractMcpMessageSupport {

    @Override
    protected ResponseEntity<Void> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        log.info("MessageHandleNode handle mcp message: {}", requestParameter.toString());

        McpSchemaVO.JsonRpcResponse jsonRpcResponse = sessionMessageService.processHandleMessage(requestParameter);
        if (null != jsonRpcResponse) {
            String responseJson = objectMapper.writeValueAsString(jsonRpcResponse);

            SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
            sessionConfigVO.getSink().tryEmitNext(
                    ServerSentEvent.<String>builder()
                            .event("message")
                            .data(responseJson)
                            .build());
        }
        return ResponseEntity.accepted().build();
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Void>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return super.get(requestParameter, dynamicContext);
    }
}
