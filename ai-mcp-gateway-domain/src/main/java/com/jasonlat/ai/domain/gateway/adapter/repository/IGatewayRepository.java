package com.jasonlat.ai.domain.gateway.adapter.repository;


import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayToolConfigCommandEntity;

/**
 * 网关仓储服务接口
 */
public interface IGatewayRepository {

    void saveGatewayConfig(GatewayConfigCommandEntity commandEntity);

    void updateGatewayConfig(GatewayConfigCommandEntity commandEntity);

    void updateGatewayAuthStatus(GatewayConfigCommandEntity commandEntity);

    void saveGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity);

    void updateGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity);

    void updateGatewayToolProtocol(GatewayToolConfigCommandEntity commandEntity);

    void deleteGatewayToolConfig(Long toolId);

}

