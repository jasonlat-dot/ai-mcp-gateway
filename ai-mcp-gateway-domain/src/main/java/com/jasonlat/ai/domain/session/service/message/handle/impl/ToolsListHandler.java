package com.jasonlat.ai.domain.session.service.message.handle.impl;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service("toolsListHandler")
public class ToolsListHandler  implements IRequestHandler {
    @Override
    public McpSchemaVO.JsonRpcResponse handleMessage(McpSchemaVO.JsonRpcRequest request) {
        log.info("开始处理工具列表请求: {}", JSON.toJSONString(request));
        McpSchemaVO.JsonRpcResponse jsonRpcResponse = new McpSchemaVO.JsonRpcResponse("2.0", request.id(), Map.of(
                "tools", new Object[]{
                        Map.of(
                                "name", "toUpperCase",
                                "description", "小写转大写",
                                "inputSchema", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "word", Map.of(
                                                        "type", "string",
                                                        "description", "单词，字符串"
                                                )
                                        ),
                                        "required", new String[]{"word"}
                                )
                        )
                }
        ), null);
        log.info("处理工具列表请求结束: {}", JSON.toJSONString(jsonRpcResponse));
        return jsonRpcResponse;

    }
}
