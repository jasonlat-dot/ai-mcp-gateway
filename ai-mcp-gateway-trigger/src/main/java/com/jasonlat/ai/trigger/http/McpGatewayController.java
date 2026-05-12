package com.jasonlat.ai.trigger.http;

import com.jasonlat.ai.cases.mcp.IMcpSessionService;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.service.ISessionMessageService;
import com.jasonlat.ai.trigger.api.IMcpGatewayService;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author jasonlat
 * 2026-04-22  20:14
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/")
public class McpGatewayController implements IMcpGatewayService {

    @Resource
    private IMcpSessionService mcpSessionService;

    @Resource
    private ISessionMessageService sessionMessageService;

    /**
     * 建立SSE连接
     * http://127.0.0.1:8888/api-gateway/test001/mcp/sse
     * @param gatewayId 网关ID
     */
    @Override
    @RequestMapping(value = "{gatewayId}/mcp/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> establishSseConnection(@PathVariable String gatewayId) {
        try {
            log.info("建立SSE连接 gatewayId:{}", gatewayId);
            if (StringUtils.isBlank(gatewayId)) {
                log.error("网关ID不能为空 gatewayId:{}", gatewayId);
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
            }
            Flux<ServerSentEvent<String>> serverSentEventFlux = mcpSessionService.establishSseConnection(gatewayId);
            log.info("建立SSE连接成功 gatewayId:{}", gatewayId);
            return serverSentEventFlux;
        } catch (Exception e) {
            log.error("建立SSE连接失败 gatewayId:{}", gatewayId, e);
            throw new RuntimeException("建立SSE连接失败: " + gatewayId, e);
        }
    }

    @Override
    @PostMapping(value = "{gatewayId}/mcp/sse", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> handleMessage(@PathVariable String gatewayId,
                                                      @RequestParam String sessionId,
                                                      @RequestBody String messageBody) {
        try {
            log.info("处理 MCP SSE 消息 gatewayId:{} sessionId:{} messageBody:{}", gatewayId, sessionId, messageBody);
            McpSchemaVO.JsonRpcRequest jsonRpcRequest =(McpSchemaVO.JsonRpcRequest) McpSchemaVO.deserializeJsonRpcMessage(messageBody);
            McpSchemaVO.JsonRpcResponse jsonRpcResponse = sessionMessageService.processHandleMessage(jsonRpcRequest);
            log.info("处理 MCP SSE 响应 gatewayId:{} sessionId:{} response:{}", gatewayId, sessionId, jsonRpcResponse);

            return Mono.just(ResponseEntity.ok().body(jsonRpcResponse));
        } catch(Exception e) {
            log.error("处理MCP SSE 消息失败 gatewayId:{}", gatewayId, e);
            return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "MCP SSE 处理消息失败")));
        }
    }


















}
