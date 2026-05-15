package com.jasonlat.ai.infrastructure.dao;

import com.jasonlat.ai.infrastructure.dao.po.McpProtocolRegistryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMcpProtocolRegistryDao {

    int insert(McpProtocolRegistryPO mcpProtocolRegistryPO);

    int deleteById(@Param("id") Long id);

    int updateById(McpProtocolRegistryPO mcpProtocolRegistryPO);

    McpProtocolRegistryPO queryById(@Param("id")Long id);

    List<McpProtocolRegistryPO> queryAll();
}

