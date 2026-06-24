package com.jasonlat.ai.domain.session.adapter.port;

import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayProtocolConfigVO;

public interface ISessionPort {


    Object toolCall(McpGatewayProtocolConfigVO.HTTPConfig httpConfig, Object params) throws Exception;
}
