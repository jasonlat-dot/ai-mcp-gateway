package com.jasonlat.ai.infrastructure.dao;

import com.jasonlat.ai.infrastructure.dao.po.McpProtocolDubboPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dubbo 协议配置 Dao,与 {@link IMcpProtocolHttpDao} 平行。
 * <p>
 * 关键方法 {@link #queryMcpProtocolDubboByProtocolId(Long)} 被
 * SessionRepository#queryMcpGatewayProtocolConfig 调用,按 protocolId 取一条记录。
 */
@Mapper
public interface IMcpProtocolDubboDao {

    int insert(McpProtocolDubboPO po);

    int deleteById(Long id);

    int updateById(McpProtocolDubboPO po);

    McpProtocolDubboPO queryById(Long id);

    List<McpProtocolDubboPO> queryAll();

    /**
     * 按 protocolId 取一条 Dubbo 配置 — 与 HTTP 那个方法对称。
     * <p>
     * 调用方保证传入的 protocolId 来自 mcp_gateway_tool.protocol_type='DUBBO' 的记录,
     * 否则可能查不到(每条 tool 只对应一种协议配置)。
     */
    McpProtocolDubboPO queryMcpProtocolDubboByProtocolId(Long protocolId);

    /**
     * 批量插入
     */
    int batchInsert(@Param("list") List<McpProtocolDubboPO> list);

    int deleteByProtocolId(Long protocolId);

    int updateByProtocolId(McpProtocolDubboPO po);

    List<McpProtocolDubboPO> queryListByProtocolIds(@Param("list") List<Long> protocolIds);

    List<McpProtocolDubboPO> queryProtocolList(McpProtocolDubboPO query);

    Long queryProtocolListCount(McpProtocolDubboPO query);
}
