package com.jasonlat.ai.cases.mcp.streamable.message.node;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonlat.ai.cases.mcp.streamable.message.AbstractMcpStreamableMessageServiceSupport;
import com.jasonlat.ai.cases.mcp.streamable.message.factory.DefaultMcpStreamableMessageFactory;
import com.jasonlat.ai.domain.auth.model.entity.LicenseCommandEntity;
import com.jasonlat.ai.domain.auth.service.IAuthLicenseService;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.enums.SessionTransportTypeEnumVO;
import com.jasonlat.ai.types.enums.McpErrorCodes;
import com.jasonlat.ai.types.exception.AppException;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.jasonlat.ai.domain.session.model.valobj.enums.SessionCustomKey.MCP_SESSION_HEADER_KEY;

/**
 * Streamable initialize 节点
 */
@Slf4j
@Service("mcpStreamableMessageInitializeNode")
public class InitializeNode extends AbstractMcpStreamableMessageServiceSupport {

    @Resource
    private IAuthLicenseService authLicenseService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected ResponseEntity<?> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpStreamableMessageFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Streamable 消息处理 InitializeNode:{}", requestParameter);

        boolean isCheckSuccess = authLicenseService.checkLicense(new LicenseCommandEntity(requestParameter.getGatewayId(), requestParameter.getApiKey()));
        if (!isCheckSuccess) {
            throw new AppException(McpErrorCodes.INSUFFICIENT_PERMISSIONS, "fail to auth apikey");
        }

        SessionConfigVO sessionConfigVO = sessionManagementService.createSession(
                requestParameter.getGatewayId(),
                requestParameter.getApiKey(),
                SessionTransportTypeEnumVO.STREAMABLE);
        dynamicContext.setSessionConfigVO(sessionConfigVO);

        McpSchemaVO.JsonRpcResponse jsonrpcResponse = serviceMessageService.processHandleMessage(requestParameter);
        String responseJson = objectMapper.writeValueAsString(jsonrpcResponse);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(MCP_SESSION_HEADER_KEY.getCode(), sessionConfigVO.getSessionId())
                .body(responseJson);
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpStreamableMessageFactory.DynamicContext, ResponseEntity<?>> get(HandleMessageCommandEntity requestParameter, DefaultMcpStreamableMessageFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

}
