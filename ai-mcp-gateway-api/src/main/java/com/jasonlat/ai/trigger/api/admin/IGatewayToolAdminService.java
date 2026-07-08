package com.jasonlat.ai.trigger.api.admin;

import com.jasonlat.ai.trigger.api.dto.GatewayConfigRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigResponseDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayToolConfigDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayToolQueryDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.trigger.api.model.ResponsePage;

import java.util.List;

/**
 * 网关工具管理 API
 * <p>对应 controller: {@code GatewayToolController} - 路径前缀 {@code /admin/tool/**}</p>
 */
public interface IGatewayToolAdminService {

    Response<GatewayConfigResponseDTO> saveGatewayToolConfig(GatewayConfigRequestDTO.GatewayToolConfig requestDTO);

    Response<GatewayConfigResponseDTO> updateGatewayToolConfig(GatewayConfigRequestDTO.GatewayToolConfig requestDTO);

    Response<GatewayConfigResponseDTO> deleteGatewayToolConfig(String gatewayId, Long toolId);

    Response<List<GatewayToolConfigDTO>> queryGatewayToolList();

    ResponsePage<List<GatewayToolConfigDTO>> queryGatewayToolPage(GatewayToolQueryDTO queryDTO);

    Response<List<GatewayToolConfigDTO>> queryGatewayToolListByGatewayId(String gatewayId);
}
