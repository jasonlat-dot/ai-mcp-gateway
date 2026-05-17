package com.jasonlat.ai.domain.session.repository;

import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;

public interface ISessionRepository {

    /**
     * 查询网关配置信息
     * @param gatewayId 网关ID
     * @return 网关配置信息
     */
    McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId);
}
