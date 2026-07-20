package com.jasonlat.ai.cases.mcp.streamable.message;

import com.jasonlat.ai.cases.mcp.IMcpMessageService;
import com.jasonlat.ai.cases.mcp.streamable.message.factory.DefaultMcpStreamableMessageFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * 会话消息处理
 */
@Slf4j
@Service
public class McpStreamableMessageService implements IMcpMessageService<String> {

    @Resource
    private DefaultMcpStreamableMessageFactory defaultMcpStreamableMessageFactory;

    @Override
    public ResponseEntity<String> handleMessage(HandleMessageCommandEntity commandEntity) throws Exception {
        StrategyHandler<HandleMessageCommandEntity, DefaultMcpStreamableMessageFactory.DynamicContext, ResponseEntity<?>> strategyHandler =
                defaultMcpStreamableMessageFactory.strategyHandler();

        ResponseEntity<?> responseEntity = strategyHandler.apply(commandEntity, new DefaultMcpStreamableMessageFactory.DynamicContext());

        return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody() == null ? null : responseEntity.getBody().toString());
    }
}
