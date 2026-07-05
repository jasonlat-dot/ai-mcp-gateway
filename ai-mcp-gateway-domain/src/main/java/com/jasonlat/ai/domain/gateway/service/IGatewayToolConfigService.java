package com.jasonlat.ai.domain.gateway.service;


import com.jasonlat.ai.domain.gateway.model.entity.GatewayToolConfigCommandEntity;

/**
 * 网关工具配置服务接口
 */
public interface IGatewayToolConfigService {

    void saveGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity);

    void updateGatewayToolProtocol(GatewayToolConfigCommandEntity commandEntity);

}
