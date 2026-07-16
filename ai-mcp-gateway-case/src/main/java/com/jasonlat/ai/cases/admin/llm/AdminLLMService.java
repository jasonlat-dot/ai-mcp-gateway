package com.jasonlat.ai.cases.admin.llm;

import com.jasonlat.ai.cases.admin.IAdminLLMService;
import com.jasonlat.ai.domain.gateway.service.IGatewayToolConfigService;
import com.jasonlat.ai.domain.llm.model.entity.BuildChatModelCommandEntity;
import com.jasonlat.ai.domain.llm.model.valobj.McpConfigVO;
import com.jasonlat.ai.domain.llm.service.ILLMService;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMResponseDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * LLM 模型对话验证case
 */
@Slf4j
@Service
public class AdminLLMService implements IAdminLLMService {

    @Value("${server.servlet.context-path}")
    private String baseUrlContextPath;

    @Value("${server.port}")
    private Integer port;

    @Resource
    private ILLMService llmService;

    @Override
    public GatewayLLMResponseDTO testCallGateway(GatewayLLMRequestDTO requestDTO) {
        log.info("AdminLLMService.testCallGateway {} {}", requestDTO.getGatewayId(), requestDTO.getMessage());

        String gatewayId = requestDTO.getGatewayId();

        String baseUrl = "http://localhost:" + port;
        String sseEndpoint = baseUrlContextPath + "/" + gatewayId + "/mcp/sse";

        McpConfigVO mcpConfigVO = McpConfigVO.builder()
                .baseUri(baseUrl)
                .sseEndpoint(sseEndpoint)
                .authApiKey(requestDTO.getAuthApiKey())
                .timeout(requestDTO.getTimeout())
                .build();

        BuildChatModelCommandEntity commandEntity = BuildChatModelCommandEntity.builder()
                .gatewayId(gatewayId)
                .mcpConfigVO(mcpConfigVO)
                .build();

        String call = llmService.callGateway(commandEntity, requestDTO.getMessage());

        return GatewayLLMResponseDTO.builder().content(call).build();
    }

}
