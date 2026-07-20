package com.jasonlat.ai.cases.admin.llm;

import com.jasonlat.ai.cases.admin.IAdminLLMService;
import com.jasonlat.ai.domain.llm.model.entity.BuildChatModelCommandEntity;
import com.jasonlat.ai.domain.llm.model.valobj.McpConfigVO;
import com.jasonlat.ai.domain.llm.model.valobj.McpTypeEnumVO;
import com.jasonlat.ai.domain.llm.service.ILLMService;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMResponseDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @Value("${mcp.internal-base-url}")
    private String mcpInternalBaseUrl;

    @Resource
    private ILLMService llmService;

    @Override
    public GatewayLLMResponseDTO testCallGateway(GatewayLLMRequestDTO requestDTO) {
        log.info("AdminLLMService.testCallGateway {} {}", requestDTO.getGatewayId(), requestDTO.getMessage());

        String gatewayId = requestDTO.getGatewayId();

        String baseUrl = StringUtils.removeEnd(mcpInternalBaseUrl, "/");

        // 解析 MCP 连接类型；默认 SSE
        McpTypeEnumVO mcpType = McpTypeEnumVO.SSE;
        if (StringUtils.isNotBlank(requestDTO.getMcpType())) {
            try {
                mcpType = McpTypeEnumVO.valueOf(requestDTO.getMcpType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("不支持的 mcpType:{}，使用默认 SSE", requestDTO.getMcpType());
            }
        }

        // 根据 MCP 类型拼接 endpoint；SSE 带 /sse 后缀，Streamable 不带
        String endpoint = baseUrlContextPath + "/" + gatewayId + "/mcp";
        if (McpTypeEnumVO.SSE == mcpType) {
            endpoint += "/sse";
        }


        McpConfigVO mcpConfigVO = McpConfigVO.builder()
                .baseUri(baseUrl)
                .endpoint(endpoint)
                .authApiKey(requestDTO.getAuthApiKey())
                .timeout(requestDTO.getTimeout())
                .build();

        BuildChatModelCommandEntity commandEntity = BuildChatModelCommandEntity.builder()
                .gatewayId(gatewayId)
                .mcpConfigVO(mcpConfigVO)
                .mcpType(mcpType)
                .build();

        String call = llmService.callGateway(commandEntity, requestDTO.getMessage());

        return GatewayLLMResponseDTO.builder().content(call).build();
    }

}
