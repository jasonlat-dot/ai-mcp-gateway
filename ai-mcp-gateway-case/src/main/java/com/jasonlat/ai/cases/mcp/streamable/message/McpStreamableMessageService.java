package com.jasonlat.ai.cases.mcp.streamable.message;

import com.jasonlat.ai.cases.mcp.IMcpMessageService;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * 会话消息处理
 */
@Slf4j
@Service
public class McpStreamableMessageService implements IMcpMessageService {

    @Override
    public ResponseEntity<Object> handleMessage(HandleMessageCommandEntity commandEntity) throws Exception {
       return null;
    }

}
