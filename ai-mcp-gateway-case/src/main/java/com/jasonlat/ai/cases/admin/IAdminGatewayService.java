package com.jasonlat.ai.cases.admin;


import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayToolConfigCommandEntity;

/**
 * 网关配置管理
 */
public interface IAdminGatewayService {

    void saveGatewayConfig(GatewayConfigCommandEntity commandEntity);

    void updateGatewayConfig(GatewayConfigCommandEntity commandEntity);

    void saveGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity);

    void updateGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity);

    void deleteGatewayToolConfig(Long toolId);

    /**
     * 删除网关基础配置;成功返回 true,不存在/已删除返回 false(幂等)
     */
    boolean deleteGatewayConfig(String gatewayId);

}
