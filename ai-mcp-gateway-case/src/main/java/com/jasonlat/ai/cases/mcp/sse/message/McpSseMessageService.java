package com.jasonlat.ai.cases.mcp.sse.message;

import com.jasonlat.ai.cases.mcp.IMcpMessageService;
import com.jasonlat.ai.cases.mcp.sse.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author jasonlat
 * 2026-06-26  19:17
 */
@Slf4j
@Service
public class McpSseMessageService implements IMcpMessageService<Object> {

    @Resource
    private DefaultMcpMessageFactory defaultMcpMessageFactory;

    @Override
    public ResponseEntity<Object> handleMessage(HandleMessageCommandEntity commandEntity) throws Exception {
        StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> strategyHandler
                = defaultMcpMessageFactory.strategyHandler();
        return strategyHandler.apply(commandEntity, new DefaultMcpMessageFactory.DynamicContext());
    }
}
