package com.jasonlat.ai.domain.session.service.message.handle.impl;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import com.jasonlat.ai.domain.session.adapter.repository.ISessionRepository;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Slf4j
@Service("initializeHandler")
public class InitializeHandler implements IRequestHandler {

    @Resource
    private ISessionRepository sessionRepository;

    /**
     * 对照 io.modelcontextprotocol.spec.McpServerSession
     * <br/>
     * McpServerSession.handle -> McpSchema.JSONRPCRequest -> handleIncomingRequest
     * -> McpSchema.METHOD_INITIALIZE -> McpAsyncServer.asyncInitializeRequestHandler
     * -> result -> new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, request.id(), result, null)
     * <br/>
     * {
     *     "id": "a355a5f7-0",
     *     "jsonrpc": "2.0",
     *     "result": {
     *         "capabilities": {
     *             "completions": {},
     *             "logging": {},
     *             "prompts": {
     *                 "listChanged": true
     *             },
     *             "resources": {
     *                 "listChanged": true,
     *                 "subscribe": false
     *             },
     *             "tools": {
     *                 "listChanged": true
     *             }
     *         },
     *         "instructions": "This server provides weather information tools and resources",
     *         "protocolVersion": "2024-11-05",
     *         "serverInfo": {
     *             "name": "ai-mcp-gateway-demo-mcp-server-test",
     *             "version": "1.0.0"
     *         }
     *     }
     * }
     */
    @Override
    public McpSchemaVO.JsonRpcResponse handleMessage(String gatewayId, McpSchemaVO.JsonRpcRequest message) {
        log.info("处理 MCP SSE 初始化请求: {}", JSON.toJSONString(message));
        McpSchemaVO.InitializeRequest initializeRequest = McpSchemaVO.unmarshalFrom(message.params(), new TypeReference<>() {
        });
        McpGatewayConfigVO mcpGatewayConfigVO = sessionRepository.queryMcpGatewayConfigByGatewayId(gatewayId);

        McpSchemaVO.InitializeResult initializeResult = new McpSchemaVO.InitializeResult(
                initializeRequest.protocolVersion(),
                new McpSchemaVO.ServerCapabilities(
                        new McpSchemaVO.ServerCapabilities.CompletionCapabilities(),
                        new HashMap<>(),
                        new McpSchemaVO.ServerCapabilities.LoggingCapabilities(),
                        new McpSchemaVO.ServerCapabilities.PromptCapabilities(true),
                        new McpSchemaVO.ServerCapabilities.ResourceCapabilities(false, true),
                        new McpSchemaVO.ServerCapabilities.ToolCapabilities(true)),
                new McpSchemaVO.Implementation(mcpGatewayConfigVO.getGatewayName(), mcpGatewayConfigVO.getVersion()),
                mcpGatewayConfigVO.getGatewayDesc());

        McpSchemaVO.JsonRpcResponse jsonRpcResponse =
                new McpSchemaVO.JsonRpcResponse(McpSchemaVO.JSONRPC_VERSION, message.id(), initializeResult, null);

        log.info("处理初始化请求结束: {}", JSON.toJSONString(jsonRpcResponse));
        return jsonRpcResponse;
    }
}
