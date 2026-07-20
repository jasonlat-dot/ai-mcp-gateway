package com.jasonlat.ai.cases.mcp;


import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import org.springframework.http.ResponseEntity;

public interface IMcpMessageService<T> {

    /**
     * 处理MCP消息
     *
     * @param messageCommandEntity 消息命令实体
     * @return 处理结果
     */
    ResponseEntity<T> handleMessage(HandleMessageCommandEntity messageCommandEntity) throws Exception;
}
