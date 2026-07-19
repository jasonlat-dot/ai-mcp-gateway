package com.jasonlat.ai.cases.mcp.sse.session.node;

import com.jasonlat.ai.cases.mcp.sse.session.AbstractMcpSseSessionSupport;
import com.jasonlat.ai.cases.mcp.sse.session.factory.DefaultMcpSessionFactory;
import com.jasonlat.ai.domain.auth.model.entity.LicenseCommandEntity;
import com.jasonlat.ai.domain.auth.service.IAuthLicenseService;
import com.jasonlat.ai.types.enums.McpErrorCodes;
import com.jasonlat.ai.types.exception.AppException;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author jasonlat
 * 2026-04-22  20:28
 */
@Slf4j
@Service("mcpSessionVerifyNode")
public class VerifyNode extends AbstractMcpSseSessionSupport {

    @Resource(name = "mcpSessionSessionNode")
    private SseSessionNode sessionNode;
    @Resource
    private IAuthLicenseService authLicenseService;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String gatewayId, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        String apiKey = dynamicContext.getValue("apiKey");
        log.info("mcpSessionVerifyNode 开始 gatewayId: {}, apiKey: {}", gatewayId, apiKey);
        boolean isCheckOk = authLicenseService.checkLicense(LicenseCommandEntity.builder()
                .gatewayId(gatewayId)
                .apiKey(apiKey).build());
        if (!isCheckOk) {
            // apiKey验证失败
            throw new AppException(McpErrorCodes.INSUFFICIENT_PERMISSIONS, "fail to auth apiKey");
        }
        return router(gatewayId, dynamicContext);

    }


    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return sessionNode;
    }
}
