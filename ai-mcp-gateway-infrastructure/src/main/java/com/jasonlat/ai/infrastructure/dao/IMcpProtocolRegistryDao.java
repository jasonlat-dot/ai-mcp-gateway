package com.jasonlat.ai.infrastructure.dao;

import com.jasonlat.ai.infrastructure.dao.po.McpProtocolRegistryPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IMcpProtocolRegistryDao {

    int insert(McpProtocolRegistryPO po);

    int deleteById(Long id);

    int updateById(McpProtocolRegistryPO po);

    McpProtocolRegistryPO queryById(Long id);

    List<McpProtocolRegistryPO> queryAll();
}

