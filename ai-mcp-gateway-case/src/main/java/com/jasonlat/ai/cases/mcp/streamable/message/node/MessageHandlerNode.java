package com.jasonlat.ai.cases.mcp.streamable.message.node;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonlat.ai.cases.mcp.streamable.message.AbstractMcpStreamableMessageServiceSupport;
import com.jasonlat.ai.cases.mcp.streamable.message.factory.DefaultMcpStreamableMessageFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

/**
 * Streamable 消息处理节点
 */
@Slf4j
@Service("mcpStreamableMessageHandlerNode")
public class MessageHandlerNode extends AbstractMcpStreamableMessageServiceSupport {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected ResponseEntity<?> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpStreamableMessageFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Streamable 消息处理 MessageHandlerNode:{}", requestParameter);

        McpSchemaVO.JsonRpcResponse jsonrpcResponse = serviceMessageService.processHandleMessage(requestParameter);
        if (null != jsonrpcResponse) {
            String responseJson = objectMapper.writeValueAsString(jsonrpcResponse);

            SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
            sessionConfigVO.getSink().tryEmitNext(ServerSentEvent.<String>builder()
                    .id(sessionConfigVO.getSessionId())
                    .event("message")
                    .data(responseJson)
                    .build());
        }

        return ResponseEntity.accepted().build();
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpStreamableMessageFactory.DynamicContext, ResponseEntity<?>> get(HandleMessageCommandEntity requestParameter, DefaultMcpStreamableMessageFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

}
