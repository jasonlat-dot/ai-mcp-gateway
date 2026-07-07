package com.jasonlat.ai.domain.auth.adapter.repository;


import com.jasonlat.ai.domain.auth.model.entity.LicenseCommandEntity;
import com.jasonlat.ai.domain.auth.model.valobj.McpGatewayAuthVO;
import com.jasonlat.ai.domain.auth.model.valobj.enums.AuthStatusEnum;

public interface IAuthRepository {
    /**
     * 查询有效的网关授权信息
     * @param licenseCommandEntity 查询授权信息命令
     * @return 网关授权信息
     */
    McpGatewayAuthVO queryEffectiveGatewayAuthInfo(LicenseCommandEntity licenseCommandEntity);

    /**
     * 查询网关授权状态
     * @param gatewayId 网关Id
     * @return 网关授权状态
     */
    AuthStatusEnum.GatewayConfig queryGatewayAuthStatus(String gatewayId);

    /**
     * 插入一条网关授权信息
     * @param mcpGatewayAuthVO 网关授权信息
     */
    void insertOne(McpGatewayAuthVO mcpGatewayAuthVO);

    /**
     * 根据网关ID更新网关授权信息
     * @param mcpGatewayAuthVO 网关授权信息（gatewayId必填）
     * @return 是否更新成功
     */
    boolean updateGatewayAuth(McpGatewayAuthVO mcpGatewayAuthVO);

    void deleteGatewayAuth(String gatewayId);
}
