package com.jasonlat.ai.infrastructure.dao;

import com.jasonlat.ai.infrastructure.dao.po.McpProtocolMappingPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMcpProtocolMappingDao {

    int insert(McpProtocolMappingPO mcpProtocolMappingPO);

    int deleteById(@Param("id") Long id);

    int updateById(McpProtocolMappingPO mcpProtocolMappingPO);

    McpProtocolMappingPO queryById(@Param("id") Long id);

    List<McpProtocolMappingPO> queryAll();
}

