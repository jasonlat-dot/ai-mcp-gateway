package com.jasonlat.ai.domain.session.service.message.handle.impl;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import com.jasonlat.ai.types.enums.McpErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service("toolsCallHandler")
public class ToolsCallHandler  implements IRequestHandler {


    @Override
    public McpSchemaVO.JsonRpcResponse handleMessage(McpSchemaVO.JsonRpcRequest request) {
        // {"id":"39eac954-2","jsonrpc":"2.0","method":"tools/call","params":{"name":"toUpperCase","arguments":{"word":"jsaonlat"}}}
        log.info("开始处理工具调用请求: {}", JSON.toJSONString(request));

        Object id = request.id();
        Object params = request.params();

        if (!(params instanceof Map)) {
            return new McpSchemaVO.JsonRpcResponse("2.0", id, null,
                    new McpSchemaVO.JsonRpcResponse.JsonRpcError(McpErrorCodes.INVALID_PARAMS, "Invalid params", null));
        }

        Map<String, Object> paramsMap = (Map<String, Object>) params;

        // 获取工具名称
        String toolName = paramsMap.get("name").toString();
        log.info("开始处理工具调用请求，工具名称：{}", toolName);

        // 获取工具参数
        Object argumentsObj = paramsMap.get("arguments");
        Map<String, Object> arguments = (Map<String, Object>) argumentsObj;
        if ("toUpperCase".equals(toolName)) {
            String word = arguments.get("word").toString();
            return new McpSchemaVO.JsonRpcResponse("2.0", id, Map.of(
                    "content", new Object[] {
                            Map.of(
                                    "type", "text",
                                    "text", word.toUpperCase()
                            )
                    }
            ), null);
        }

        return null;
    }















}
