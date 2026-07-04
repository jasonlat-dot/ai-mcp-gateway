package com.jasonlat.ai.infrastructure.adapter.repository;


import com.jasonlat.ai.domain.protocol.adapter.repository.IProtocolRepository;
import com.jasonlat.ai.domain.protocol.model.valobj.enums.ProtocolStatusEnum;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.infrastructure.dao.IMcpProtocolHttpDao;
import com.jasonlat.ai.infrastructure.dao.IMcpProtocolMappingDao;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolHttpPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolMappingPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 协议仓储服务
 */
@Slf4j
@Repository
public class ProtocolRepository implements IProtocolRepository {

    @Resource
    private IMcpProtocolHttpDao protocolHttpDao;

    @Resource
    private IMcpProtocolMappingDao protocolMappingDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveHttpProtocolAndMapping(List<HTTPProtocolVO> httpProtocolVOS) {

        if (null == httpProtocolVOS || httpProtocolVOS.isEmpty()) return new ArrayList<>();

        List<Long> protocolIdList = new ArrayList<>();

        List<McpProtocolHttpPO> mcpProtocolHttpPOs = new ArrayList<>(httpProtocolVOS.size());
        for (HTTPProtocolVO httpProtocolVO : httpProtocolVOS) {

            // 0. 生成协议ID，12位数字的。
            long protocolId = Long.parseLong(RandomStringUtils.randomNumeric(12));

            // 1. 保存 HTTP 协议配置
            McpProtocolHttpPO mcpProtocolHttpPO = McpProtocolHttpPO.builder()
                    .protocolId(protocolId)
                    .httpUrl(httpProtocolVO.getHttpUrl())
                    .httpMethod(httpProtocolVO.getHttpMethod())
                    .httpHeaders(httpProtocolVO.getHttpHeaders())
                    .timeout(httpProtocolVO.getTimeout())
                    .retryTimes(3)
                    .status(ProtocolStatusEnum.ENABLE.getCode())
                    .build();
            mcpProtocolHttpPOs.add(mcpProtocolHttpPO);

            // 2. 保存协议映射配置
            List<HTTPProtocolVO.ProtocolMapping> mappings = httpProtocolVO.getMappings();
            if (null == mappings || mappings.isEmpty()) continue;

            List<McpProtocolMappingPO> mcpProtocolMappingPOs = new ArrayList<>(mappings.size());
            for (HTTPProtocolVO.ProtocolMapping mapping : mappings) {
                McpProtocolMappingPO mcpProtocolMappingPO = McpProtocolMappingPO.builder()
                        .protocolId(protocolId)
                        .mappingType(mapping.getMappingType())
                        .parentPath(mapping.getParentPath())
                        .fieldName(mapping.getFieldName())
                        .mcpPath(mapping.getMcpPath())
                        .mcpType(mapping.getMcpType())
                        .mcpDesc(mapping.getMcpDesc())
                        .isRequired(mapping.getIsRequired())
                        .sortOrder(mapping.getSortOrder())
                        .build();
                mcpProtocolMappingPOs.add(mcpProtocolMappingPO);
            }
            protocolMappingDao.batchInsert(mcpProtocolMappingPOs);

            protocolIdList.add(protocolId);
        }

        // 批量保存
        protocolHttpDao.batchInsert(mcpProtocolHttpPOs);
        return protocolIdList;
    }

}
