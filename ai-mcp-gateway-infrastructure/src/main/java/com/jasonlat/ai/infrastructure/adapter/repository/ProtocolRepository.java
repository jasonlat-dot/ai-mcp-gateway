package com.jasonlat.ai.infrastructure.adapter.repository;


import com.jasonlat.ai.domain.protocol.adapter.repository.IProtocolRepository;
import com.jasonlat.ai.domain.protocol.model.valobj.dubbo.DubboProtocolVO;
import com.jasonlat.ai.domain.protocol.model.valobj.enums.ProtocolStatusEnum;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.infrastructure.dao.IMcpProtocolDubboDao;
import com.jasonlat.ai.infrastructure.dao.IMcpProtocolHttpDao;
import com.jasonlat.ai.infrastructure.dao.IMcpProtocolMappingDao;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolDubboPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolHttpPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolMappingPO;
import com.jasonlat.ai.types.snow.SnowflakeIdGenerator;
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
    private IMcpProtocolDubboDao protocolDubboDao;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Resource
    private IMcpProtocolMappingDao protocolMappingDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveHttpProtocolAndMapping(List<HTTPProtocolVO> httpProtocolVOS) {

        if (null == httpProtocolVOS || httpProtocolVOS.isEmpty()) return new ArrayList<>();

        List<Long> protocolIdList = new ArrayList<>();

        List<McpProtocolHttpPO> mcpProtocolHttpPOs = new ArrayList<>(httpProtocolVOS.size());
        for (HTTPProtocolVO httpProtocolVO : httpProtocolVOS) {

            // 0. 生成协议ID
            long protocolId = snowflakeIdGenerator.nextId();

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateHttpProtocolAndMapping(HTTPProtocolVO httpProtocolVO) {
        if (null == httpProtocolVO || httpProtocolVO.getProtocolId() == null) return false;

        // 1. 更新 HTTP 协议配置
        McpProtocolHttpPO mcpProtocolHttpPO = McpProtocolHttpPO.builder()
                .protocolId(httpProtocolVO.getProtocolId())
                .httpUrl(httpProtocolVO.getHttpUrl())
                .httpMethod(httpProtocolVO.getHttpMethod())
                .httpHeaders(httpProtocolVO.getHttpHeaders())
                .timeout(httpProtocolVO.getTimeout())
                .build();
        int httpCount = protocolHttpDao.updateByProtocolId(mcpProtocolHttpPO);
        if (1 != httpCount) {
            return false;
        }

        // 2. 先删除该协议原有映射再批量插入新映射
        protocolMappingDao.deleteByProtocolId(httpProtocolVO.getProtocolId());
        List<HTTPProtocolVO.ProtocolMapping> mappings = httpProtocolVO.getMappings();
        if (null != mappings && !mappings.isEmpty()) {
            List<McpProtocolMappingPO> mcpProtocolMappingPOs = new ArrayList<>(mappings.size());
            for (HTTPProtocolVO.ProtocolMapping mapping : mappings) {
                McpProtocolMappingPO mcpProtocolMappingPO = McpProtocolMappingPO.builder()
                        .protocolId(httpProtocolVO.getProtocolId())
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
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteGatewayProtocol(Long protocolId) {
        // 两表都尝试删,protocolId 命中哪个就删哪个 — 单协议不会同时占两行。
        protocolHttpDao.deleteByProtocolId(protocolId);
        protocolDubboDao.deleteByProtocolId(protocolId);
        protocolMappingDao.deleteByProtocolId(protocolId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveDubboProtocolAndMapping(List<DubboProtocolVO> dubboProtocolVOS) {
        if (null == dubboProtocolVOS || dubboProtocolVOS.isEmpty()) return new ArrayList<>();

        List<Long> protocolIdList = new ArrayList<>(dubboProtocolVOS.size());

        for (DubboProtocolVO vo : dubboProtocolVOS) {
            // 0. 生成协议ID
            long protocolId = snowflakeIdGenerator.nextId();

            // 1. 保存 Dubbo 协议配置
            McpProtocolDubboPO dubboPO = McpProtocolDubboPO.builder()
                    .protocolId(protocolId)
                    .interfaceName(vo.getInterfaceName())
                    .groupName(vo.getGroupName())
                    .version(vo.getVersion())
                    .methodName(vo.getMethodName())
                    .parameterTypes(com.alibaba.fastjson.JSON.toJSONString(vo.getParameterTypes()))
                    .timeout(vo.getTimeout() == null ? 3000 : vo.getTimeout())
                    .retryTimes(vo.getRetryTimes() == null ? 0 : vo.getRetryTimes())
                    .directUrl(vo.getDirectUrl())
                    .directEnabled(vo.getDirectEnabled())
                    .status(vo.getStatus() == null
                            ? ProtocolStatusEnum.ENABLE.getCode()
                            : vo.getStatus())
                    .build();
            protocolDubboDao.insert(dubboPO);

            // 2. 保存协议映射配置
            List<DubboProtocolVO.ProtocolMapping> mappings = vo.getMappings();
            if (mappings != null && !mappings.isEmpty()) {
                List<McpProtocolMappingPO> mappingPOs = new ArrayList<>(mappings.size());
                for (DubboProtocolVO.ProtocolMapping m : mappings) {
                    mappingPOs.add(McpProtocolMappingPO.builder()
                            .protocolId(protocolId)
                            .mappingType(m.getMappingType())
                            .parentPath(m.getParentPath())
                            .fieldName(m.getFieldName())
                            .mcpPath(m.getMcpPath())
                            .mcpType(m.getMcpType())
                            .mcpDesc(m.getMcpDesc())
                            .isRequired(m.getIsRequired())
                            .sortOrder(m.getSortOrder())
                            .build());
                }
                protocolMappingDao.batchInsert(mappingPOs);
            }

            protocolIdList.add(protocolId);
        }

        log.info("Dubbo 协议保存完成: count={}", protocolIdList.size());
        return protocolIdList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDubboProtocolAndMapping(DubboProtocolVO vo) {
        if (vo == null || vo.getProtocolId() == null) return false;

        // 1. 更新 mcp_protocol_dubbo 行(只更新允许编辑的字段,interfaceName 不让改)
        McpProtocolDubboPO po = McpProtocolDubboPO.builder()
                .protocolId(vo.getProtocolId())
                .groupName(vo.getGroupName())
                .version(vo.getVersion())
                .methodName(vo.getMethodName())
                .parameterTypes(com.alibaba.fastjson.JSON.toJSONString(vo.getParameterTypes()))
                .timeout(vo.getTimeout() == null ? 3000 : vo.getTimeout())
                .retryTimes(vo.getRetryTimes() == null ? 0 : vo.getRetryTimes())
                .directUrl(vo.getDirectUrl())
                .directEnabled(vo.getDirectEnabled() == null ? 0 : vo.getDirectEnabled())
                .status(vo.getStatus())
                .build();
        int count = protocolDubboDao.updateByProtocolId(po);
        if (count != 1) {
            return false;
        }

        // 2. 重置 mapping 行
        protocolMappingDao.deleteByProtocolId(vo.getProtocolId());
        List<DubboProtocolVO.ProtocolMapping> mappings = vo.getMappings();
        if (mappings != null && !mappings.isEmpty()) {
            List<McpProtocolMappingPO> mappingPOs = new ArrayList<>(mappings.size());
            for (DubboProtocolVO.ProtocolMapping m : mappings) {
                mappingPOs.add(McpProtocolMappingPO.builder()
                        .protocolId(vo.getProtocolId())
                        .mappingType(m.getMappingType())
                        .parentPath(m.getParentPath())
                        .fieldName(m.getFieldName())
                        .mcpPath(m.getMcpPath())
                        .mcpType(m.getMcpType())
                        .mcpDesc(m.getMcpDesc())
                        .isRequired(m.getIsRequired())
                        .sortOrder(m.getSortOrder())
                        .build());
            }
            protocolMappingDao.batchInsert(mappingPOs);
        }
        return true;
    }

}