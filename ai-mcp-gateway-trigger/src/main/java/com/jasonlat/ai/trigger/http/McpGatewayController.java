package com.jasonlat.ai.trigger.http;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.cases.mcp.IMcpMessageService;
import com.jasonlat.ai.cases.mcp.IMcpSessionService;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;

import com.jasonlat.ai.trigger.api.IMcpGatewayService;
import com.jasonlat.ai.trigger.api.model.Response;
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

import java.util.UUID;

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
    private IMcpMessageService mcpMessageService;

    /**
     * 建立SSE连接
     * http://127.0.0.1:8888/api-gateway/test001/mcp/sse
     * @param gatewayId 网关ID
     */
    @Override
    @RequestMapping(value = "{gatewayId}/mcp/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> establishSseConnection(@PathVariable("gatewayId") String gatewayId,
                                                                @RequestParam(
                                                                        value = "api_key",
                                                                        required = false,
                                                                        defaultValue = ""
                                                                ) String apiKey) throws Exception {
        try {
            log.info("建立SSE连接 gatewayId:{}", gatewayId);
            if (StringUtils.isBlank(gatewayId)) {
                log.error("网关ID不能为空 gatewayId:{}", gatewayId);
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
            }
            Flux<ServerSentEvent<String>> serverSentEventFlux = mcpSessionService.establishSseConnection(gatewayId, apiKey);
            log.info("建立SSE连接成功 gatewayId:{}", gatewayId);
            return serverSentEventFlux;
        }  catch (AppException e) {
            log.error("建立 MCP SSE 连接拒绝，gatewayId: {}", gatewayId, e);
            return Flux.just(ServerSentEvent.<String>builder()
                    .id(UUID.randomUUID().toString())
                    .event("error")
                    .data(JSON.toJSONString(Response.<String>builder()
                            .code(e.getCode())
                            .info(e.getInfo())
                            .build()))
                    .build());
        } catch (Exception e) {
            log.error("建立 MCP SSE 连接失败，gatewayId: {}", gatewayId, e);
            throw e;
        }
    }

    @Override
    @PostMapping(value = "{gatewayId}/mcp/sse", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> handleMessage(@PathVariable("gatewayId") String gatewayId,
                                                    @RequestParam(value = "api_key", required = false, defaultValue = "") String apiKey,
                                                    @RequestParam("sessionId") String sessionId,
                                                    @RequestBody String messageBody) throws Exception {
        try {
            log.info("处理 MCP SSE 消息 gatewayId:{} sessionId:{} messageBody:{}", gatewayId, sessionId, messageBody);

            // 非法参数校验
            if (StringUtils.isBlank(gatewayId) || StringUtils.isBlank(sessionId)) {
                log.error("参数不能为空 gatewayId:{} sessionId:{}", gatewayId, sessionId);
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
            }

            HandleMessageCommandEntity handleMessageCommandEntity = new HandleMessageCommandEntity(gatewayId, apiKey, sessionId, messageBody);
            ResponseEntity<Object> responseEntity = mcpMessageService.handleMessage(handleMessageCommandEntity);

            log.info("处理 MCP SSE 消息成功 gatewayId:{} sessionId:{} messageBody:{}", gatewayId, sessionId, messageBody);
            return Mono.just(responseEntity);
        } catch (AppException e) {
            log.error("处理 MCP SSE 消息被拒绝，gatewayId: {}", gatewayId, e);
            return Mono.just(ResponseEntity.badRequest().body(e.getMessage()));
        } catch (Exception e) {
            log.error("处理 MCP SSE 消息失败，gatewayId: {}", gatewayId, e);
            throw e;
        }
    }


















}
