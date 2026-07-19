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
public class McpSseMessageService implements IMcpMessageService {

    @Resource
    private DefaultMcpMessageFactory defaultMcpMessageFactory;

    /**
     * 处理MCP消息
     *
     * @param messageCommandEntity 消息命令实体
     * @return 处理结果
     */
    @Override
    public ResponseEntity<Object> handleMessage(HandleMessageCommandEntity messageCommandEntity) throws Exception {
        StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> strategyHandler
                = defaultMcpMessageFactory.strategyHandler();
        // 调用策略处理逻辑
        return strategyHandler.apply(messageCommandEntity, new DefaultMcpMessageFactory.DynamicContext());
    }
}
