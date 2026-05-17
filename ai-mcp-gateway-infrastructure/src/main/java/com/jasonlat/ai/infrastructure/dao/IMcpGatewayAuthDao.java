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
}

