package com.jasonlat.ai.domain.session.service.message.handle.impl;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jasonlat.ai.domain.session.adapter.port.ISessionPort;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.domain.session.adapter.repository.ISessionRepository;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import com.jasonlat.ai.types.enums.McpErrorCodes;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Slf4j
@Service("toolsCallHandler")
public class ToolsCallHandler  implements IRequestHandler {

    @Resource
    private ISessionRepository repository;
    @Resource
    private ISessionPort port;

    @Override
    public McpSchemaVO.JsonRpcResponse handleMessage(String gatewayId, McpSchemaVO.JsonRpcRequest message) {
        try {
            // {"id":"39eac954-2","jsonrpc":"2.0","method":"tools/call","params":{"name":"toUpperCase","arguments":{"word":"jsaonlat"}}}
            log.info("ToolsCallHandler 开始处理工具调用请求: {}", JSON.toJSONString(message));

            // 获取工具调用参数 params
            McpSchemaVO.CallToolRequest callToolRequest = McpSchemaVO.unmarshalFrom(message.params(), new TypeReference<>() {
            });

            Map<String, Object> argumentsMap = callToolRequest.arguments();
            String toolName = callToolRequest.name();
            McpToolProtocolConfigVO mcpToolProtocolConfigVO = repository.queryMcpGatewayProtocolConfig(gatewayId, toolName);
            if (mcpToolProtocolConfigVO == null) {
                log.error("工具调用失败: 工具不存在, gatewayId:{} toolName:{}", gatewayId, toolName);
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
            }
            Object result = port.toolCall(mcpToolProtocolConfigVO, argumentsMap);
            log.info("工具调用结果 result: {}", result);
            return new McpSchemaVO.JsonRpcResponse(McpSchemaVO.JSONRPC_VERSION, message.id(), Map.of(
                    "content", List.of(
                            Map.of(
                                    "type", "text",
                                    "text", result
                            )
                    ),
                    "isError", false
            ), null);


        } catch (Exception e) {
            log.error("处理工具调用请求异常: {}", e.getMessage(), e);
            return new McpSchemaVO.JsonRpcResponse(McpSchemaVO.JSONRPC_VERSION, message.id(), null,
                    new McpSchemaVO.JsonRpcResponse.JsonRpcError(McpErrorCodes.INTERNAL_ERROR, e.getMessage(), null));
        }
    }















}
