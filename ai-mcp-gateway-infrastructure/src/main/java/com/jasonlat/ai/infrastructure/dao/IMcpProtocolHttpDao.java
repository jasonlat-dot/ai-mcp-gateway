package com.jasonlat.ai.infrastructure.dao;

import com.jasonlat.ai.infrastructure.dao.po.McpProtocolHttpPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMcpProtocolHttpDao {

    int insert(McpProtocolHttpPO po);

    int deleteById(Long id);

    int updateById(McpProtocolHttpPO po);

    McpProtocolHttpPO queryById(Long id);

    List<McpProtocolHttpPO> queryAll();

    McpProtocolHttpPO queryMcpProtocolHttpByProtocolId(Long protocolId);

    /**
     * 批量插入
     * @param list 数据列表
     * @return 受影响行数
     */
    int batchInsert(@Param("list") List<McpProtocolHttpPO> list);
}

