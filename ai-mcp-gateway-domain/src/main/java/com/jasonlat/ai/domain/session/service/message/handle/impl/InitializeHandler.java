package com.jasonlat.ai.domain.session.service.message.handle.impl;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service("initializeHandler")
public class InitializeHandler implements IRequestHandler {
    @Override
    public McpSchemaVO.JsonRpcResponse handleMessage(McpSchemaVO.JsonRpcRequest message) {
        log.info("模拟处理初始化请求: {}", JSON.toJSONString(message));
        McpSchemaVO.JsonRpcResponse jsonRpcResponse = new McpSchemaVO.JsonRpcResponse("2.0", message.id(), Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of(
                        "tools", Map.of(),
                        "resources", Map.of()
                ),
                "serverInfo", Map.of(
                        "name", "MCP Word Util Proxy Server",
                        "version", "1.0.0"
                )
        ), null);

        log.info("模拟处理初始化请求结束: {}", JSON.toJSONString(jsonRpcResponse));
        return jsonRpcResponse;
    }
}
