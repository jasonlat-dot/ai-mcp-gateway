package com.jasonlat.ai.trigger.api.admin;

import com.jasonlat.ai.trigger.api.dto.GatewayConfigQueryDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigResponseDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.trigger.api.model.ResponsePage;

import java.util.List;

/**
 * 网关基础配置管理 API
 * <p>对应 controller: {@code GatewayController} - 路径前缀 {@code /admin/gateway/**}</p>
 */
public interface IGatewayAdminService {

    Response<GatewayConfigResponseDTO> saveGatewayConfig(GatewayConfigRequestDTO.GatewayConfig requestDTO);

    Response<GatewayConfigResponseDTO> updateGatewayConfig(GatewayConfigRequestDTO.GatewayConfig requestDTO);

    Response<GatewayConfigResponseDTO> deleteGatewayConfig(String gatewayId);

    Response<List<GatewayConfigDTO>> queryGatewayConfigList();

    ResponsePage<List<GatewayConfigDTO>> queryGatewayConfigPage(GatewayConfigQueryDTO queryDTO);
}
