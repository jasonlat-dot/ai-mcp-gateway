package com.jasonlat.ai.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolMappingPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolRegistryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IMcpProtocolRegistryDao extends BaseMapper<McpProtocolRegistryPO> {

    int insert(McpProtocolRegistryPO mcpProtocolRegistryPO);

    int deleteById(@Param("id") Long id);

    int updateById(McpProtocolRegistryPO mcpProtocolRegistryPO);

    McpProtocolRegistryPO queryById(@Param("id")Long id);

    List<McpProtocolRegistryPO> queryAll();

    McpProtocolRegistryPO queryMcpProtocolRegistryByGatewayId(@Param("gatewayId") String gatewayId);
}

