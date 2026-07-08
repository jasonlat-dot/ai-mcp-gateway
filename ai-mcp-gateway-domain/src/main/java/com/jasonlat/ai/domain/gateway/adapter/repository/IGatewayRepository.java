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

    /**
     * 按 gatewayId 删除网关基础配置
     * <p>返回影响行数:0=不存在(已被他人删除),1=删除成功</p>
     */
    int deleteGatewayConfigByGatewayId(String gatewayId);

    /**
     * 统计某个网关下挂载的工具数量(精确匹配 gateway_id)
     * <p>用于删除网关前的引用校验</p>
     */
    long countToolsByGatewayId(String gatewayId);

    /**
     * 统计某个网关下挂载的 API Key 数量(精确匹配 gateway_id,包含所有状态)
     * <p>用于删除网关前的引用校验。已禁用的 auth 也算引用,避免遗留孤儿数据</p>
     */
    long countAuthsByGatewayId(String gatewayId);

}

