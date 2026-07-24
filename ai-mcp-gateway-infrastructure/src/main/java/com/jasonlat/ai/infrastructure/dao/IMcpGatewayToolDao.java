package com.jasonlat.ai.infrastructure.dao;

import com.jasonlat.ai.infrastructure.dao.po.McpGatewayToolPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMcpGatewayToolDao {

    List<McpGatewayToolPO> queryEffectiveTools(String gatewayId);

    Long queryToolProtocolIdByToolName(McpGatewayToolPO mcpGatewayToolPOReq);

    /**
     * 按 (gatewayId, toolName) 取完整工具 PO — 用于 tools/call 路由时
     * 同时拿到 protocol_id + protocol_type,以决定加载哪种协议配置表。
     * <p>
     * 旧方法 {@link #queryToolProtocolIdByToolName} 只返回 protocolId,
     * 拿不到 protocolType,无法支撑多协议路由,只保留给历史使用方。
     */
    McpGatewayToolPO queryByGatewayIdAndToolName(@Param("gatewayId") String gatewayId,
                                                 @Param("toolName") String toolName);

    void insert(McpGatewayToolPO mcpGatewayToolPO);

    int updateProtocolByGatewayId(McpGatewayToolPO mcpGatewayToolPO);

    int updateToolConfigByToolId(McpGatewayToolPO mcpGatewayToolPO);


    List<McpGatewayToolPO> queryListByGatewayId(String gatewayId);


    List<McpGatewayToolPO> queryToolList(McpGatewayToolPO query);

    Long queryToolListCount(McpGatewayToolPO query);

    List<McpGatewayToolPO> queryAll();

    int deleteByToolId(Long toolId);

    /**
     * 精确等值统计某网关下的工具数量
     * <p>用于删除网关前的引用校验</p>
     */
    long countByGatewayId(@Param("gatewayId") String gatewayId);
}
