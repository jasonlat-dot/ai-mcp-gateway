package com.jasonlat.ai.trigger.api.admin;

import com.jasonlat.ai.trigger.api.dto.GatewayConfigRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigResponseDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayProtocolDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayProtocolQueryDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.trigger.api.model.ResponsePage;

import java.util.List;

/**
 * 网关协议管理 API
 * <p>对应 controller: {@code GatewayProtocolController} - 路径前缀 {@code /admin/protocol/**}</p>
 */
public interface IGatewayProtocolAdminService {

    Response<GatewayConfigResponseDTO> saveGatewayProtocol(GatewayConfigRequestDTO.GatewayProtocol requestDTO);

    Response<GatewayConfigResponseDTO> updateGatewayProtocol(GatewayConfigRequestDTO.GatewayProtocol requestDTO);

    Response<GatewayConfigResponseDTO> importGatewayProtocol(GatewayConfigRequestDTO.GatewayProtocolImport requestDTO);

    Response<List<GatewayProtocolDTO>> analysisProtocol(GatewayConfigRequestDTO.GatewayProtocolImport requestDTO);

    Response<GatewayConfigResponseDTO> deleteGatewayProtocol(Long protocolId);

    Response<List<GatewayProtocolDTO>> queryGatewayProtocolList();

    ResponsePage<List<GatewayProtocolDTO>> queryGatewayProtocolPage(GatewayProtocolQueryDTO queryDTO);

    Response<List<GatewayProtocolDTO>> queryGatewayProtocolListByGatewayId(String gatewayId);
}
