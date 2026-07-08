package com.jasonlat.ai.trigger.api.admin;

import com.jasonlat.ai.trigger.api.dto.GatewayAuthDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayAuthQueryDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigResponseDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.trigger.api.model.ResponsePage;

import java.util.List;

/**
 * 网关认证管理 API
 * <p>对应 controller: {@code GatewayAuthController} - 路径前缀 {@code /admin/auth/**}</p>
 */
public interface IGatewayAuthAdminService {

    Response<GatewayConfigResponseDTO> saveGatewayAuth(GatewayConfigRequestDTO.GatewayAuth requestDTO);

    Response<GatewayConfigResponseDTO> updateGatewayAuth(GatewayConfigRequestDTO.GatewayAuth requestDTO);

    Response<GatewayConfigResponseDTO> deleteGatewayAuth(String gatewayId);

    Response<List<GatewayAuthDTO>> queryGatewayAuthList();

    ResponsePage<List<GatewayAuthDTO>> queryGatewayAuthPage(GatewayAuthQueryDTO queryDTO);
}
