package com.jasonlat.ai.cases.admin;

import com.jasonlat.ai.trigger.api.dto.GatewayLLMRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMResponseDTO;

public interface IAdminLLMService {

    GatewayLLMResponseDTO testCallGateway(GatewayLLMRequestDTO requestDTO);

}