package com.jasonlat.ai.domain.session.service.message.handle.impl;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.domain.session.adapter.repository.ISessionRepository;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;


@Slf4j
@Service("toolsListHandler")
public class ToolsListHandler  implements IRequestHandler {

    @Resource
    private ISessionRepository sessionRepository;

    @Override
    public McpSchemaVO.JsonRpcResponse handleMessage(String gatewayId, McpSchemaVO.JsonRpcRequest request) {

        log.info("ToolsListHandler 开始处理工具列表请求: {}", JSON.toJSONString(request));

        // 查询网关下工具列表配置
        List<McpToolConfigVO> mcpGatewayToolConfigs = sessionRepository.queryMcpGatewayToolConfigListByGatewayId(gatewayId);

        List<McpSchemaVO.Tool> tools = buildTools(mcpGatewayToolConfigs);

        McpSchemaVO.JsonRpcResponse jsonRpcResponse = new McpSchemaVO.JsonRpcResponse(
                "2.0",
                request.id(),
                Map.of("tools",  tools),
                null);
        log.info("ToolsListHandler 处理工具列表请求结束: {}", JSON.toJSONString(jsonRpcResponse));
        return jsonRpcResponse;

    }


    private List<McpSchemaVO.Tool> buildTools(List<McpToolConfigVO> toolConfigs) {
        List<McpSchemaVO.Tool> tools = new ArrayList<>();

        for (McpToolConfigVO toolConfigVO : toolConfigs) {
            McpToolProtocolConfigVO mcpToolProtocolConfigVO = toolConfigVO.getMcpToolProtocolConfigVO();
            List<McpToolProtocolConfigVO.ProtocolMapping> configs = mcpToolProtocolConfigVO.getRequestProtocolMappings();

            // 排序
            configs.sort(Comparator.comparingInt(McpToolProtocolConfigVO.ProtocolMapping::getSortOrder));

            // 父子元素 Map parentPath -> List<Children>
            Map<String, List<McpToolProtocolConfigVO.ProtocolMapping>> childrenMap = new HashMap<>();

            List<McpToolProtocolConfigVO.ProtocolMapping> roots = new ArrayList<>();

            for (McpToolProtocolConfigVO.ProtocolMapping config : configs) {
                if (config.getParentPath() == null) {
                    roots.add(config);
                } else {
                    childrenMap.computeIfAbsent(config.getParentPath(), k -> new ArrayList<>()).add(config);
                }
            }

            // 排序
            roots.sort(Comparator.comparingInt(McpToolProtocolConfigVO.ProtocolMapping::getSortOrder));

            // 构建输入结构
            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();

            for (McpToolProtocolConfigVO.ProtocolMapping root : roots) {
                properties.put(root.getFieldName(), buildProperty(root, childrenMap));
                if (Integer.valueOf(1).equals(root.getIsRequired())) {
                    required.add(root.getFieldName());
                }
            }

            // 获取类型
            String type = roots.size() == 1 ? roots.get(0).getMcpType() : "object";

            // 构造函数
            McpSchemaVO.JsonSchema inputSchema = new McpSchemaVO.JsonSchema(
                    type,
                    properties,
                    required.isEmpty() ? null : required,
                    false,
                    null,
                    null
            );

            // 工具描述
            tools.add(new McpSchemaVO.Tool(toolConfigVO.getToolName(), toolConfigVO.getToolDescription(), inputSchema));
        }

        return tools;
    }

    private Map<String, Object> buildProperty(McpToolProtocolConfigVO.ProtocolMapping current, Map<String, List<McpToolProtocolConfigVO.ProtocolMapping>> childrenMap) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", current.getMcpType());
        if (current.getMcpDesc() != null) {
            property.put("description", current.getMcpDesc());
        }

        // 校验孩子元素
        List<McpToolProtocolConfigVO.ProtocolMapping> children = childrenMap.get(current.getMcpPath());
        if (children != null && !children.isEmpty()) {
            Map<String, Object> props = new HashMap<>();
            List<String> reqs = new ArrayList<>();

            // 排序
            children.sort(Comparator.comparingInt(McpToolProtocolConfigVO.ProtocolMapping::getSortOrder));

            for (McpToolProtocolConfigVO.ProtocolMapping child : children) {
                // 注意，buildProperty 嵌套递归，一层层的寻找，是否还有孩子元素（children）
                props.put(child.getFieldName(), buildProperty(child, childrenMap));
                if (Integer.valueOf(1).equals(child.getIsRequired())) {
                    reqs.add(child.getFieldName());
                }
            }

            property.put("properties", props);

            if (!reqs.isEmpty()) {
                property.put("required", reqs);
            }

        }

        return property;
    }




















}
