package com.jasonlat.ai.domain.gateway.service;


import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;

/**
 * 网关配置接口
 */
public interface IGatewayConfigService {

    void saveGatewayConfig(GatewayConfigCommandEntity commandEntity);

    void updateGatewayConfig(GatewayConfigCommandEntity commandEntity);

    void updateGatewayAuthStatus(GatewayConfigCommandEntity commandEntity);

}
