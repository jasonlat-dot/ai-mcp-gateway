package com.jasonlat.ai.trigger.api;


import com.jasonlat.ai.trigger.api.dto.*;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.trigger.api.model.ResponsePage;

import java.util.List;

/**
 * 运营配置管理服务接口
 */
public interface IAdminService {

    Response<GatewayConfigResponseDTO> saveGatewayConfig(GatewayConfigRequestDTO.GatewayConfig requestDTO);

    Response<GatewayConfigResponseDTO> updateGatewayConfig(GatewayConfigRequestDTO.GatewayConfig requestDTO);

    Response<GatewayConfigResponseDTO> saveGatewayToolConfig(GatewayConfigRequestDTO.GatewayToolConfig requestDTO);

    Response<GatewayConfigResponseDTO> updateGatewayToolConfig(GatewayConfigRequestDTO.GatewayToolConfig requestDTO);

    Response<GatewayConfigResponseDTO> saveGatewayProtocol(GatewayConfigRequestDTO.GatewayProtocol requestDTO);

    Response<GatewayConfigResponseDTO> updateGatewayProtocol(GatewayConfigRequestDTO.GatewayProtocol requestDTO);

    Response<GatewayConfigResponseDTO> importGatewayProtocol(GatewayConfigRequestDTO.GatewayProtocolImport requestDTO);

    Response<List<GatewayProtocolDTO>> analysisProtocol(GatewayConfigRequestDTO.GatewayProtocolImport requestDTO);

    Response<GatewayConfigResponseDTO> saveGatewayAuth(GatewayConfigRequestDTO.GatewayAuth requestDTO);

    Response<GatewayConfigResponseDTO> updateGatewayAuth(GatewayConfigRequestDTO.GatewayAuth requestDTO);

    Response<List<GatewayConfigDTO>> queryGatewayConfigList();

    ResponsePage<List<GatewayConfigDTO>> queryGatewayConfigPage(GatewayConfigQueryDTO queryDTO);

    Response<List<GatewayToolConfigDTO>> queryGatewayToolList();

    ResponsePage<List<GatewayToolConfigDTO>> queryGatewayToolPage(GatewayToolQueryDTO queryDTO);

    Response<List<GatewayToolConfigDTO>> queryGatewayToolListByGatewayId(String gatewayId);

    Response<List<GatewayProtocolDTO>> queryGatewayProtocolList();

    ResponsePage<List<GatewayProtocolDTO>> queryGatewayProtocolPage(GatewayProtocolQueryDTO queryDTO);

    Response<List<GatewayProtocolDTO>> queryGatewayProtocolListByGatewayId(String gatewayId);

    Response<List<GatewayAuthDTO>> queryGatewayAuthList();

    ResponsePage<List<GatewayAuthDTO>> queryGatewayAuthPage(GatewayAuthQueryDTO queryDTO);

    Response<GatewayConfigResponseDTO> deleteGatewayToolConfig(String gatewayId, Long toolId);

}
