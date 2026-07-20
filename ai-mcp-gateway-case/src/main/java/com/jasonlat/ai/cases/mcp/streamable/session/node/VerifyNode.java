package com.jasonlat.ai.cases.mcp.streamable.session.node;


import com.jasonlat.ai.cases.mcp.streamable.session.AbstractMcpStreamableSessionSupport;
import com.jasonlat.ai.cases.mcp.streamable.session.factory.DefaultMcpStreamableSessionFactory;
import com.jasonlat.ai.domain.auth.model.entity.LicenseCommandEntity;
import com.jasonlat.ai.domain.auth.service.IAuthLicenseService;
import com.jasonlat.ai.types.enums.McpErrorCodes;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


/**
 * 验证节点
 */
@Slf4j
@Service("mcpStreamableSessionVerifyNode")
public class VerifyNode extends AbstractMcpStreamableSessionSupport {

    @Resource(name = "mcpStreamableSessionNode")
    private StreamableSessionNode sessionNode;

    @Resource
    private IAuthLicenseService authLicenseService;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpStreamableSessionFactory.DynamicContext dynamicContext) throws Exception {
        log.info("获取 Streamable 会话-VerifyNode gatewayId:{} sessionId:{}", dynamicContext.getGatewayId(), requestParameter);

        boolean isCheckSuccess = authLicenseService.checkLicense(LicenseCommandEntity.builder()
                .apiKey(dynamicContext.getApiKey())
                .gatewayId(dynamicContext.getGatewayId())
                .build());
        if (!isCheckSuccess) {
            throw new AppException(McpErrorCodes.INSUFFICIENT_PERMISSIONS, "fail to auth apiKey");
        }
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<String, DefaultMcpStreamableSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpStreamableSessionFactory.DynamicContext dynamicContext) throws Exception {
        return sessionNode;
    }

}
