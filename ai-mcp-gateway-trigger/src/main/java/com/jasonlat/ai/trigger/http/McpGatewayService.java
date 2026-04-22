package com.jasonlat.ai.trigger.http;

import com.jasonlat.ai.cases.mcp.IMcpSessionService;
import com.jasonlat.ai.trigger.api.IMcpGatewayService;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author jasonlat
 * 2026-04-22  20:14
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/")
public class McpGatewayService implements IMcpGatewayService {

    @Resource
    private IMcpSessionService mcpSessionService;

    /**
     * 建立SSE连接
     * http://127.0.0.1:8888/api-gateway/test001/mcp/sse
     * @param gatewayId 网关ID
     */
    @Override
    @RequestMapping("{gatewayId}/mcp/sse")
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
}
