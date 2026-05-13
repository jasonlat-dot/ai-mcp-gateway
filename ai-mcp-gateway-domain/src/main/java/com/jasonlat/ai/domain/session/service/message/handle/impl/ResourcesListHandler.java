package com.jasonlat.ai.domain.session.service.message.handle.impl;


import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service("resourcesListHandler")
public class ResourcesListHandler implements IRequestHandler {
    @Override
    public McpSchemaVO.JsonRpcResponse handleMessage(McpSchemaVO.JsonRpcRequest request) {
        log.info("模拟处理资源列表请求: {}", JSON.toJSONString(request));
        McpSchemaVO.JsonRpcResponse jsonRpcResponse = new McpSchemaVO.JsonRpcResponse("2.0", request.id(), Map.of(
                "resources", Map.of(
                        "resources", new Object[]{}
                )
        ), null);

        log.info("模拟处理资源列表请求结束: {}", JSON.toJSONString(jsonRpcResponse));
        return jsonRpcResponse;
    }
}
