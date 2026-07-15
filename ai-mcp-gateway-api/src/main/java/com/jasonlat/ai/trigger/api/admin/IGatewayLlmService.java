package com.jasonlat.ai.trigger.api.admin;

import com.jasonlat.ai.trigger.api.dto.GatewayLLMRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMResponseDTO;
import com.jasonlat.ai.trigger.api.model.Response;

public interface IGatewayLlmService {

    Response<GatewayLLMResponseDTO> testCallGateway(GatewayLLMRequestDTO requestDTO);
}
