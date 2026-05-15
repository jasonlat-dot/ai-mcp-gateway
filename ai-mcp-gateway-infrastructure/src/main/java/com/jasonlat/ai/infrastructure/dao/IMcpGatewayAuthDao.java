package com.jasonlat.ai.infrastructure.dao;

import com.jasonlat.ai.infrastructure.dao.po.McpGatewayAuthPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMcpGatewayAuthDao {

    int insert(McpGatewayAuthPO mcpGatewayAuthPO);

    int deleteById(@Param("id") Long id);

    int updateById(McpGatewayAuthPO mcpGatewayAuthPO);

    McpGatewayAuthPO queryById(@Param("id")Long id);

    List<McpGatewayAuthPO> queryAll();
}

