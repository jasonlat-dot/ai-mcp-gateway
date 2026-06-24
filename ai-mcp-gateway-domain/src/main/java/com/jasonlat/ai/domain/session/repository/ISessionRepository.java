package com.jasonlat.ai.domain.session.repository;

import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;

import java.util.List;

public interface ISessionRepository {

    /**
     * 查询网关配置信息
     * @param gatewayId 网关ID
     * @return 网关配置信息
     */
    McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId);

    /**
     * 网关协议映射
     * @param gatewayId 网关ID
     * @return 网关协议映射
     */
    List<McpToolConfigVO> queryMcpGatewayToolConfigListByGatewayId(String gatewayId);

    /**
     * 查询协议配置信息
     * @param gatewayId 网关ID
     * toolName 工具名称
     * @return 网关协议配置信息
     */
    McpToolProtocolConfigVO queryMcpGatewayProtocolConfig(String gatewayId, String toolName);
}
