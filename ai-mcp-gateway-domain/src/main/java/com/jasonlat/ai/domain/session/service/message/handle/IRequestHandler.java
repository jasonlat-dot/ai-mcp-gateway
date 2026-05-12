package com.jasonlat.ai.domain.session.service.message.handle;

import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;

/**
 * @author jasonlat
 * 2026-05-12  22:20
 */
public interface IRequestHandler {
    McpSchemaVO.JsonRpcResponse handleMessage(McpSchemaVO.JsonRpcRequest request);

}
