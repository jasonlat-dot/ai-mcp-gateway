package com.jasonlat.ai.domain.session.repository;

import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayToolConfigVO;

import java.util.List;

public interface ISessionRepository {

    /**
     * 查询网关配置信息
     * @param gatewayId 网关ID
     * @return 网关配置信息
     */
    McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId);

    /**
     * 查询网关下工具列表配置
     * @param gatewayId 网关ID
     * @return 网关下工具列表配置
     */
    List<McpGatewayToolConfigVO> queryMcpGatewayToolConfigListByGatewayId(String gatewayId);
}
