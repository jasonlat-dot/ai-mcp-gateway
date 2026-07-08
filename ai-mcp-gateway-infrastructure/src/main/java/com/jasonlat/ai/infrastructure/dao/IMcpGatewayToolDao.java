package com.jasonlat.ai.infrastructure.dao;

import com.jasonlat.ai.infrastructure.dao.po.McpGatewayToolPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMcpGatewayToolDao {

    List<McpGatewayToolPO> queryEffectiveTools(String gatewayId);

    Long queryToolProtocolIdByToolName(McpGatewayToolPO mcpGatewayToolPOReq);

    void insert(McpGatewayToolPO mcpGatewayToolPO);

    int updateProtocolByGatewayId(McpGatewayToolPO mcpGatewayToolPO);

    int updateToolConfigByToolId(McpGatewayToolPO mcpGatewayToolPO);


    List<McpGatewayToolPO> queryListByGatewayId(String gatewayId);


    List<McpGatewayToolPO> queryToolList(McpGatewayToolPO query);

    Long queryToolListCount(McpGatewayToolPO query);

    List<McpGatewayToolPO> queryAll();

    int deleteByToolId(Long toolId);

    /**
     * 精确等值统计某网关下的工具数量
     * <p>用于删除网关前的引用校验</p>
     */
    long countByGatewayId(@Param("gatewayId") String gatewayId);
}
