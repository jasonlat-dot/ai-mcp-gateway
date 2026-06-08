package com.jasonlat.ai.domain.session.service.message.handle.impl;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayToolConfigVO;
import com.jasonlat.ai.domain.session.repository.ISessionRepository;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service("toolsListHandler")
public class ToolsListHandler  implements IRequestHandler {

    @Resource
    private ISessionRepository sessionRepository;

    @Override
    public McpSchemaVO.JsonRpcResponse handleMessage(String gatewayId, McpSchemaVO.JsonRpcRequest request) {
        log.info("开始处理工具列表请求: {}", JSON.toJSONString(request));

        // 1. 查询网关配置信息
        McpGatewayConfigVO mcpGatewayConfig = sessionRepository.queryMcpGatewayConfigByGatewayId(gatewayId);
        // 2. 查询网关下工具列表配置
        List<McpGatewayToolConfigVO> mcpGatewayToolConfigs = sessionRepository.queryMcpGatewayToolConfigListByGatewayId(gatewayId);

        List<McpSchemaVO.Tool> tools = buildTools(mcpGatewayConfig, mcpGatewayToolConfigs);

        McpSchemaVO.JsonRpcResponse jsonRpcResponse = new McpSchemaVO.JsonRpcResponse(
                "2.0",
                request.id(),
                Map.of("tools",  tools),
                null);
        log.info("处理工具列表请求结束: {}", JSON.toJSONString(jsonRpcResponse));
        return jsonRpcResponse;

    }

    private List<McpSchemaVO.Tool> buildTools(McpGatewayConfigVO mcpGatewayConfig, List<McpGatewayToolConfigVO> toolConfigs) {
        // 1. 根据工具ID进行分组
        Map<Long, List<McpGatewayToolConfigVO>> toolsMap = toolConfigs.stream().collect(Collectors.groupingBy(McpGatewayToolConfigVO::getToolId));

        List<McpSchemaVO.Tool> tools = new ArrayList<>(toolConfigs.size());
        for (Map.Entry<Long, List<McpGatewayToolConfigVO>> entry : toolsMap.entrySet()) {
            Long toolId = entry.getKey();
            List<McpGatewayToolConfigVO> configs = entry.getValue();
            // 排序 防止乱序
            configs.sort(Comparator.comparingInt(McpGatewayToolConfigVO::getSortOrder));

            Map<String, List<McpGatewayToolConfigVO>> childMap = new HashMap<>();
            List<McpGatewayToolConfigVO> roots = new ArrayList<>();
            for (McpGatewayToolConfigVO config : configs) {
                if (config.getParentPath() == null) {
                    roots.add(config);
                } else {
                    childMap.computeIfAbsent(config.getParentPath(), k -> new ArrayList<>()).add(config);
                }
            }
            // 给根节点排序
            roots.sort(Comparator.comparingInt(McpGatewayToolConfigVO::getSortOrder));

            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();
            for (McpGatewayToolConfigVO root : roots) {
                properties.put(root.getFieldName(), buildProperty(root, childMap));
                if (Integer.valueOf(1).equals(root.getIsRequired())) {
                    required.add(root.getFieldName());
                }
            }

            String type = roots.size() == 1 ? roots.get(0).getMcpType() : "object";

            McpSchemaVO.JsonSchema inputJsonSchema = new McpSchemaVO.JsonSchema(
                    type,
                    properties,
                    required.isEmpty() ? null : required,
                    false,
                    null,
                    null
            );

            String name = "unknown-tool-" + toolId, dec = "";
            if (mcpGatewayConfig != null && Objects.equals(mcpGatewayConfig.getToolId(), toolId)) {
                name = mcpGatewayConfig.getToolName();
                dec = mcpGatewayConfig.getToolDesc();
            }
            // 构建工具
            tools.add(new McpSchemaVO.Tool(name, dec,inputJsonSchema));
        }

        return tools;
    }

    private Map<String, Object> buildProperty(McpGatewayToolConfigVO current, Map<String,List<McpGatewayToolConfigVO>> childrenMap) {
        Map<String, Object> currentProperties = new HashMap<>();
        currentProperties.put("type", current.getMcpType());
        if (current.getMcpDesc() != null) {
            currentProperties.put("description", current.getMcpDesc());
        }
        // 寻找 current 的子节点
        List<McpGatewayToolConfigVO> children = childrenMap.get(current.getMcpPath());
        if (null != children && !children.isEmpty()) {
            Map<String, Object> childrenProperties = new HashMap<>();
            List<String> reqs = new ArrayList<>();
            // 排序
            children.sort(Comparator.comparingInt(McpGatewayToolConfigVO::getSortOrder));

            for (McpGatewayToolConfigVO child : children) {
                childrenProperties.put(child.getFieldName(), buildProperty(child, childrenMap));
                if (Integer.valueOf(1).equals(child.getIsRequired())) {
                    reqs.add(child.getFieldName());
                }
            }
            currentProperties.put("properties", childrenProperties);
            if (!reqs.isEmpty()) {
                currentProperties.put("required", reqs);
            }
        }
        return currentProperties;
    }




















}
