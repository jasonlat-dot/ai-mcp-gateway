package com.jasonlat.ai.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolMappingPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IMcpProtocolMappingDao extends BaseMapper<McpProtocolMappingPO> {

    int insert(McpProtocolMappingPO mcpProtocolMappingPO);

    int deleteById(@Param("id") Long id);

    int updateById(McpProtocolMappingPO mcpProtocolMappingPO);

    McpProtocolMappingPO queryById(@Param("id") Long id);

    List<McpProtocolMappingPO> queryAll();

    List<McpProtocolMappingPO> queryMcpGatewayToolConfigListByProtocolId(@Param("protocolId") Long protocolId);

    /**
     * 批量插入协议映射数据
     * @param list 实体集合
     * @return 影响数据库行数
     */
    int batchInsert(@Param("list") List<McpProtocolMappingPO> list);


    List<McpProtocolMappingPO> queryListByProtocolIds(List<Long> protocolIds);

    void deleteByProtocolId(@Param("protocolId") Long protocolId);
}

