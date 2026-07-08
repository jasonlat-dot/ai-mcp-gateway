package com.jasonlat.ai.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayAuthPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IMcpGatewayAuthDao extends BaseMapper<McpGatewayAuthPO> {

    int insert(McpGatewayAuthPO mcpGatewayAuthPO);

    int deleteById(@Param("id") Long id);

    int updateById(McpGatewayAuthPO mcpGatewayAuthPO);

    McpGatewayAuthPO queryById(@Param("id")Long id);

    List<McpGatewayAuthPO> queryAll();

    McpGatewayAuthPO queryMcpGatewayAuthPO(McpGatewayAuthPO poReq);

    int deleteByGatewayId(String gatewayId);

    int updateByGatewayId(McpGatewayAuthPO po);

    List<McpGatewayAuthPO> queryAuthList(McpGatewayAuthPO query);

    Long queryAuthListCount(McpGatewayAuthPO query);

    int queryEffectiveGatewayAuthCount(String gatewayId);

    /**
     * 精确等值统计某网关下的全部 API Key 数量(包含所有 status)
     * <p>用于删除网关前的引用校验:已禁用的 auth 也算引用,避免遗留孤儿数据</p>
     */
    long countByGatewayId(@Param("gatewayId") String gatewayId);
}

