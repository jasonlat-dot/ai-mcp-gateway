package com.jasonlat.ai.domain.session.service;

import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;

public interface ISessionMessageService {

    McpSchemaVO.JsonRpcResponse processHandleMessage(String gatewayId, McpSchemaVO.JsonRpcMessage request);
}
