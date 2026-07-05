package com.jasonlat.ai.cases.admin;


import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayToolConfigCommandEntity;

/**
 * 网关配置管理
 */
public interface IAdminGatewayService {

    void saveGatewayConfig(GatewayConfigCommandEntity commandEntity);

    void saveGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity);

}
