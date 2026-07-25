package com.jasonlat.ai.infrastructure.adapter.repository;

import com.jasonlat.ai.domain.session.model.valobj.enums.ProtocolType;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.domain.session.adapter.repository.ISessionRepository;
import com.jasonlat.ai.infrastructure.dao.*;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayPO;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayToolPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolDubboPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolHttpPO;
import com.jasonlat.ai.infrastructure.dao.po.McpProtocolMappingPO;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Resource
    private IMcpProtocolDubboDao mcpProtocolDubboDao;

    @Resource
    private ObjectMapper objectMapper;

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
        // 1. 一次查询拿到 (protocolId, protocolType) — 旧版只查 protocolId,无法支撑多协议路由
        McpGatewayToolPO tool = mcpGatewayToolDao.queryByGatewayIdAndToolName(gatewayId, toolName);
        if (tool == null) {
            log.warn("工具不存在,gatewayId:{} toolName:{}", gatewayId, toolName);
            return null;
        }
        Long protocolId = tool.getProtocolId();
        String protocolType = tool.getProtocolType();
        if (protocolType == null || protocolType.isBlank()) {
            // 兼容老数据:protocol_type 为空时按 HTTP 处理
            protocolType = ProtocolType.HTTP.getValue();
        }

        // 2. 按 protocolType 分发加载具体协议配置
        McpToolProtocolConfigVO.McpToolProtocolConfigVOBuilder builder = McpToolProtocolConfigVO.builder()
                .protocolType(ProtocolType.get(protocolType));

        if (ProtocolType.HTTP.getValue().equals(protocolType)) {
            McpProtocolHttpPO http = mcpProtocolHttpDao.queryMcpProtocolHttpByProtocolId(protocolId);
            if (http == null) {
                log.warn("HTTP 协议配置缺失,protocolId:{}", protocolId);
                return null;
            }
            builder.httpConfig(McpToolProtocolConfigVO.HTTPConfig.builder()
                    .url(http.getHttpUrl())
                    .headers(http.getHttpHeaders())
                    .method(http.getHttpMethod())
                    .timeoutMs(http.getTimeout())
                    .build());
        } else if (ProtocolType.DUBBO.getValue().equals(protocolType)) {
            McpProtocolDubboPO dubbo = mcpProtocolDubboDao.queryMcpProtocolDubboByProtocolId(protocolId);
            if (dubbo == null) {
                log.warn("DUBBO 协议配置缺失,protocolId:{}", protocolId);
                return null;
            }
            // parameter_types 是 JSON 字符串,反序列化给业务层用 List<String>
            List<String> paramTypes;
            try {
                paramTypes = (dubbo.getParameterTypes() == null || dubbo.getParameterTypes().isBlank())
                        ? Collections.emptyList()
                        : objectMapper.readValue(dubbo.getParameterTypes(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.error("DUBBO parameter_types JSON 解析失败,protocolId:{} raw:{}",
                        protocolId, dubbo.getParameterTypes(), e);
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
            }
            builder.dubboConfig(McpToolProtocolConfigVO.DubboConfig.builder()
                    .interfaceName(dubbo.getInterfaceName())
                    .group(dubbo.getGroupName())
                    .version(dubbo.getVersion())
                    .methodName(dubbo.getMethodName())
                    .parameterTypes(paramTypes)
                    .timeoutMs(dubbo.getTimeout())
                    .retries(dubbo.getRetryTimes())
                    .directUrls(splitDirectUrls(dubbo.getDirectUrl()))
                    .directEnabled(dubbo.getDirectEnabled())
                    .build());
        } else {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }

        // 3. 字段映射表共用 — 与具体协议无关,schema 描述的是 MCP 客户端入参/出参
        List<McpProtocolMappingPO> mappingPOList =
                mcpProtocolMappingDao.queryMcpGatewayToolConfigListByProtocolId(protocolId);
        List<McpToolProtocolConfigVO.ProtocolMapping> requestProtocolMappings = new ArrayList<>(mappingPOList.size());
        for (McpProtocolMappingPO mcpProtocolMappingPO : mappingPOList) {
            requestProtocolMappings.add(McpToolProtocolConfigVO.ProtocolMapping.builder()
                    .mappingType(mcpProtocolMappingPO.getMappingType())
                    .parentPath(mcpProtocolMappingPO.getParentPath())
                    .fieldName(mcpProtocolMappingPO.getFieldName())
                    .mcpPath(mcpProtocolMappingPO.getMcpPath())
                    .mcpType(mcpProtocolMappingPO.getMcpType())
                    .mcpDesc(mcpProtocolMappingPO.getMcpDesc())
                    .isRequired(mcpProtocolMappingPO.getIsRequired())
                    .sortOrder(mcpProtocolMappingPO.getSortOrder())
                    .build());
        }
        builder.requestProtocolMappings(requestProtocolMappings);

        return builder.build();
    }

    /**
     * 把数据库存的"英文逗号分隔直连 URL 字符串"解析为 List<String>。
     * <p>
     * 设计要点:
     * - 入参为 null / 空白时返回空列表(由 DubboInvoker 决定是否走 Nacos)。
     * - 元素按 trim + 过滤空串处理,避免运营误填 "a, b, ,c" 导致 NPE 或空请求。
     * - 不在此处做 URL 格式校验,留给 DubboInvoker 在调用时检测,这样 SQL 录入时
     *   不会因为一个手抖的空格导致整条配置都保存失败。
     */
    private static List<String> splitDirectUrls(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        String[] parts = raw.split(",");
        List<String> result = new ArrayList<>(parts.length);
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
