package com.jasonlat.ai.infrastructure.adapter.repository;

import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayToolConfigVO;
import com.jasonlat.ai.domain.session.repository.ISessionRepository;
import com.jasonlat.ai.infrastructure.dao.IMcpGatewayDao;
import com.jasonlat.ai.infrastructure.dao.IMcpProtocolMappingDao;
import com.jasonlat.ai.infrastructure.dao.IMcpProtocolRegistryDao;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolMappingPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolRegistryPO;
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
    private IMcpProtocolRegistryDao mcpProtocolRegistryDao;

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

        McpProtocolRegistryPO mcpProtocolRegistryPO = mcpProtocolRegistryDao.queryMcpProtocolRegistryByGatewayId(gatewayId);
        if (null == mcpProtocolRegistryPO) return null;

        return McpGatewayConfigVO.builder()
                .gatewayId(mcpGatewayPO.getGatewayId())
                .gatewayName(mcpGatewayPO.getGatewayName())
                .toolId(mcpProtocolRegistryPO.getToolId())
                .toolName(mcpProtocolRegistryPO.getToolName())
                .toolDesc(mcpProtocolRegistryPO.getToolDescription())
                .toolVersion(mcpProtocolRegistryPO.getToolVersion())
                .build();
    }

    /**
     * 查询网关下工具列表配置
     * @param gatewayId 网关ID
     * @return 网关下工具列表配置
     */
    @Override
    public List<McpGatewayToolConfigVO> queryMcpGatewayToolConfigListByGatewayId(String gatewayId) {
        McpProtocolMappingPO reqPO = new McpProtocolMappingPO();
        reqPO.setGatewayId(gatewayId);

        List<McpProtocolMappingPO> mcpProtocolMappingPOList = mcpProtocolMappingDao.queryMcpGatewayToolConfigList(reqPO);
        if (null == mcpProtocolMappingPOList || mcpProtocolMappingPOList.isEmpty()) {
            return Collections.emptyList();
        }
        List<McpGatewayToolConfigVO> mcpGatewayToolConfigVOList = new ArrayList<>(mcpProtocolMappingPOList.size());
        for (McpProtocolMappingPO mcpProtocolMappingPO : mcpProtocolMappingPOList) {
            McpGatewayToolConfigVO mcpGatewayToolConfigVO = McpGatewayToolConfigVO.builder()
                        .gatewayId(mcpProtocolMappingPO.getGatewayId())
                        .toolId(mcpProtocolMappingPO.getToolId())
                        .mappingType(mcpProtocolMappingPO.getMappingType())
                        .parentPath(mcpProtocolMappingPO.getParentPath())
                        .fieldName(mcpProtocolMappingPO.getFieldName())
                        .mcpPath(mcpProtocolMappingPO.getMcpPath())
                        .mcpType(mcpProtocolMappingPO.getMcpType())
                        .mcpDesc(mcpProtocolMappingPO.getMcpDesc())
                        .isRequired(mcpProtocolMappingPO.getIsRequired())
                        .sortOrder(mcpProtocolMappingPO.getSortOrder())
                        .build();
            mcpGatewayToolConfigVOList.add(mcpGatewayToolConfigVO);
        }
        return mcpGatewayToolConfigVOList;
    }
}
