package com.jasonlat.ai.infrastructure.adapter.repository;

import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.domain.session.adapter.repository.ISessionRepository;
import com.jasonlat.ai.infrastructure.dao.*;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayPO;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayToolPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolMappingPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolHttpPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jasonlat
 * 2026-05-17  15:06
 */
@Slf4j
@Repository
public class SessionRepository implements ISessionRepository {

    @Resource
    private IMcpGatewayDao mcpGatewayDao;

    @Resource
    private IMcpProtocolHttpDao mcpProtocolHttpDao;

    @Resource
    private IMcpGatewayToolDao mcpGatewayToolDao;

    @Resource
    private IMcpProtocolMappingDao mcpProtocolMappingDao;

    /**
     * 查询网关配置信息
     * @param gatewayId 网关ID
     * @return 网关配置信息
     */
    @Override
    public McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId) {
        McpGatewayPO mcpGatewayPO = mcpGatewayDao.queryMcpGatewayByGatewayId(gatewayId);
        if (null == mcpGatewayPO) return null;

        return McpGatewayConfigVO.builder()
                .gatewayId(mcpGatewayPO.getGatewayId())
                .gatewayName(mcpGatewayPO.getGatewayName())
                .gatewayDesc(mcpGatewayPO.getGatewayDesc())
                .version(mcpGatewayPO.getVersion())
                .build();
    }

    /**
     * 查询网关下工具列表配置
     * @param gatewayId 网关ID
     * @return 网关下工具列表配置
     */
    @Override
    public List<McpToolConfigVO> queryMcpGatewayToolConfigListByGatewayId(String gatewayId) {


        // 1. 查询工具列表
        List<McpGatewayToolPO> mcpGatewayToolPOList = mcpGatewayToolDao.queryEffectiveTools(gatewayId);
        if (null == mcpGatewayToolPOList || mcpGatewayToolPOList.isEmpty()) return Collections.emptyList();

        List<McpToolConfigVO> mcpToolConfigVOS = new ArrayList<>(mcpGatewayToolPOList.size());
        // 2. 组装参数信息
        for (McpGatewayToolPO tool : mcpGatewayToolPOList) {

            // todo 不要在循环中查询数据 后续优化
            List<McpProtocolMappingPO> mappingPOList = mcpProtocolMappingDao.queryMcpGatewayToolConfigListByProtocolId(tool.getProtocolId());

            List<McpToolProtocolConfigVO.ProtocolMapping> requestProtocolMappings = new ArrayList<>(mappingPOList.size());

            // 协议信息
            for (McpProtocolMappingPO mcpProtocolMappingPO : mappingPOList) {
                McpToolProtocolConfigVO.ProtocolMapping protocolMapping = McpToolProtocolConfigVO.ProtocolMapping.builder()
                        .mappingType(mcpProtocolMappingPO.getMappingType())
                        .parentPath(mcpProtocolMappingPO.getParentPath())
                        .fieldName(mcpProtocolMappingPO.getFieldName())
                        .mcpPath(mcpProtocolMappingPO.getMcpPath())
                        .mcpType(mcpProtocolMappingPO.getMcpType())
                        .mcpDesc(mcpProtocolMappingPO.getMcpDesc())
                        .isRequired(mcpProtocolMappingPO.getIsRequired())
                        .sortOrder(mcpProtocolMappingPO.getSortOrder())
                        .build();
                requestProtocolMappings.add(protocolMapping);
            }

            // 组装数据
            McpToolConfigVO toolConfigVO = McpToolConfigVO.builder()
                    .gatewayId(tool.getGatewayId())
                    .toolId(tool.getToolId())
                    .toolName(tool.getToolName())
                    .toolDescription(tool.getToolDescription())
                    .toolVersion(tool.getToolVersion())
                    .mcpToolProtocolConfigVO(McpToolProtocolConfigVO.builder()
                            .requestProtocolMappings(requestProtocolMappings)
                            .build())
                    .build();

            mcpToolConfigVOS.add(toolConfigVO);
        }

        return mcpToolConfigVOS;
    }

    @Override
    public McpToolProtocolConfigVO queryMcpGatewayProtocolConfig(String gatewayId, String toolName) {
        // 获取协议ID - 根据网关ID + 工具名称
        McpGatewayToolPO mcpGatewayToolPOReq = new McpGatewayToolPO();
        mcpGatewayToolPOReq.setGatewayId(gatewayId);
        mcpGatewayToolPOReq.setToolName(toolName);
        Long protocolId = mcpGatewayToolDao.queryToolProtocolIdByToolName(mcpGatewayToolPOReq);

        // 查询协议
        McpProtocolHttpPO mcpProtocolHttpPO = mcpProtocolHttpDao.queryMcpProtocolHttpByProtocolId(protocolId);
        if (null == mcpProtocolHttpPO) return null;

        McpToolProtocolConfigVO.HTTPConfig httpConfig = new McpToolProtocolConfigVO.HTTPConfig();
        httpConfig.setUrl(mcpProtocolHttpPO.getHttpUrl());
        httpConfig.setHeaders(mcpProtocolHttpPO.getHttpHeaders());
        httpConfig.setMethod(mcpProtocolHttpPO.getHttpMethod());
        httpConfig.setTimeoutMs(mcpProtocolHttpPO.getTimeout());

        return McpToolProtocolConfigVO.builder().httpConfig(httpConfig).build();
    }
}
