package com.jasonlat.ai.trigger.http;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonlat.ai.cases.mcp.IMcpSessionService;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.ai.domain.session.service.ISessionManagementService;
import com.jasonlat.ai.domain.session.service.ISessionMessageService;
import com.jasonlat.ai.trigger.api.IMcpGatewayService;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    // todo 暂时调用 domain 测试，后续调用 case 编排
    @Resource
    private ISessionMessageService sessionMessageService;

    @Resource
    private ISessionManagementService sessionManagementService;

    @Resource
    private ObjectMapper objectMapper;

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

            SessionConfigVO session = sessionManagementService.getSession(sessionId);
            if (session == null) {
                log.error("会话不存在 gatewayId:{} sessionId:{}", gatewayId, sessionId);
                return Mono.just(ResponseEntity.notFound().build());
            }

            McpSchemaVO.JsonRpcMessage jsonRpcRequest = McpSchemaVO.deserializeJsonRpcMessage(messageBody);
            log.info("序列化消息：{}", JSON.toJSONString(jsonRpcRequest));

            McpSchemaVO.JsonRpcResponse jsonRpcResponse = sessionMessageService.processHandleMessage(jsonRpcRequest);
            if (null != jsonRpcResponse) {
                String responseJson = objectMapper.writeValueAsString(jsonRpcResponse);
                session.getSink().tryEmitNext(
                        ServerSentEvent.<String>builder()
                                .event("message")
                                .data(responseJson)
                                .build());
            }
            return Mono.just(ResponseEntity.accepted().build());
        } catch(Exception e) {
            log.error("处理MCP SSE 消息失败 gatewayId:{}", gatewayId, e);
            return Mono.just(ResponseEntity.internalServerError().build());
        }
    }


















}
