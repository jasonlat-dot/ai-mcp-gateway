package com.jasonlat.ai.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IMcpGatewayDao extends BaseMapper<McpGatewayPO> {

    int insert(McpGatewayPO mcpGatewayPO);

    int deleteById(@Param("id")Long id);

    int updateById(McpGatewayPO mcpGatewayPO);

    McpGatewayPO queryById(@Param("id")Long id);

    List<McpGatewayPO> queryAll();

    McpGatewayPO queryMcpGatewayByGatewayId(@Param("gatewayId")String gatewayId);

    int updateAuthStatusByGatewayId(McpGatewayPO mcpGatewayPO, @Param("newVersion")String newVersion);
}

