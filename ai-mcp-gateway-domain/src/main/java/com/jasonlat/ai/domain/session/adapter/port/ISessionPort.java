package com.jasonlat.ai.domain.session.adapter.port;

import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;

public interface ISessionPort {


    Object toolCall(McpToolProtocolConfigVO.HTTPConfig httpConfig, Object params) throws Exception;
}
