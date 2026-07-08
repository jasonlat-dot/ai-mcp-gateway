package com.jasonlat.ai.domain.gateway.service;


import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;

/**
 * 网关配置接口
 */
public interface IGatewayConfigService {

    void saveGatewayConfig(GatewayConfigCommandEntity commandEntity);

    void updateGatewayConfig(GatewayConfigCommandEntity commandEntity);

    void updateGatewayAuthStatus(GatewayConfigCommandEntity commandEntity);

    /**
     * 删除网关基础配置
     * <p>成功返回 true;gatewayId 不存在/已删除时返回 false(视为幂等成功,不抛异常)</p>
     */
    boolean deleteGatewayConfig(String gatewayId);

}
