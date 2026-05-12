package com.jasonlat.ai.domain.session.service.message.handle.impl;


import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service("resourcesListHandler")
public class ResourcesListHandler implements IRequestHandler {
    @Override
    public McpSchemaVO.JsonRpcResponse handleMessage(McpSchemaVO.JsonRpcRequest request) {
        return null;
    }
}
