package com.jasonlat.ai.infrastructure.dao;

import com.jasonlat.ai.infrastructure.dao.po.McpGatewayPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMcpGatewayDao {

    int insert(McpGatewayPO mcpGatewayPO);

    int deleteById(@Param("id")Long id);

    int updateById(McpGatewayPO mcpGatewayPO);

    McpGatewayPO queryById(@Param("id")Long id);

    List<McpGatewayPO> queryAll();
}

