package com.jasonlat.ai.trigger.http;


import com.jasonlat.ai.cases.admin.IAdminGatewayAuthService;
import com.jasonlat.ai.cases.admin.IAdminGatewayService;
import com.jasonlat.ai.cases.admin.IAdminManageService;
import com.jasonlat.ai.cases.admin.IAdminProtocolService;
import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigEntity;
import com.jasonlat.ai.domain.auth.model.entity.RegisterCommandEntity;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayToolConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.valobj.GatewayConfigVO;
import com.jasonlat.ai.domain.gateway.model.valobj.GatewayToolConfigVO;
import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.trigger.api.IAdminService;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigResponseDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.types.enums.GatewayEnum;
import com.jasonlat.ai.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 运营配置管理服务
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/admin/")
public class AdminController implements IAdminService {

    @Resource
    private IAdminGatewayService adminGatewayService;
    @Resource
    private IAdminGatewayAuthService adminAuthService;
    @Resource
    private IAdminProtocolService adminProtocolService;
    @Resource
    private IAdminManageService adminManageService;

    @RequestMapping(value = "save_gateway_config", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> saveGatewayConfig(@RequestBody GatewayConfigRequestDTO.GatewayConfig requestDTO) {
        try {
            log.info("保存网关配置开始 gatewayId: {}", requestDTO.getGatewayId());
            GatewayConfigCommandEntity commandEntity = GatewayConfigCommandEntity.builder()
                    .gatewayConfigVO(GatewayConfigVO.builder()
                            .gatewayId(requestDTO.getGatewayId())
                            .gatewayName(requestDTO.getGatewayName())
                            .gatewayDesc(requestDTO.getGatewayDesc())
                            .version(requestDTO.getVersion())
                            .auth(GatewayEnum.GatewayAuthStatusEnum.get(requestDTO.getAuth()))
                            .status(GatewayEnum.GatewayStatus.getByCode(requestDTO.getStatus()))
                            .build())
                    .build();
            adminGatewayService.saveGatewayConfig(commandEntity);
            log.info("保存网关配置完成 gatewayId: {}", requestDTO.getGatewayId());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (Exception e) {
            log.error("保存网关配置失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "save_gateway_tool_config", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> saveGatewayToolConfig(@RequestBody GatewayConfigRequestDTO.GatewayToolConfig requestDTO) {
        try {
            log.info("保存网关工具配置开始 gatewayId: {}", requestDTO.getGatewayId());
            GatewayToolConfigCommandEntity commandEntity = GatewayToolConfigCommandEntity.builder()
                    .gatewayToolConfigVO(GatewayToolConfigVO.builder()
                            .gatewayId(requestDTO.getGatewayId())
                            .toolId(requestDTO.getToolId())
                            .toolName(requestDTO.getToolName())
                            .toolType(requestDTO.getToolType())
                            .toolDescription(requestDTO.getToolDescription())
                            .toolVersion(requestDTO.getToolVersion())
                            .protocolId(requestDTO.getProtocolId())
                            .protocolType(requestDTO.getProtocolType())
                            .build())
                    .build();
            adminGatewayService.saveGatewayToolConfig(commandEntity);
            log.info("保存网关工具配置完成 gatewayId: {}", requestDTO.getGatewayId());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (Exception e) {
            log.error("保存网关工具配置失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "save_gateway_protocol", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> saveGatewayProtocol(@RequestBody GatewayConfigRequestDTO.GatewayProtocol requestDTO) {
        try {
            log.info("保存网关协议配置开始");
            StorageCommandEntity commandEntity = new StorageCommandEntity();
            if (requestDTO.getHttpProtocols() != null) {
                commandEntity.setHttpProtocolVOS(requestDTO.getHttpProtocols().stream().map(p -> {
                    HTTPProtocolVO vo = new HTTPProtocolVO();
                    vo.setProtocolId(p.getProtocolId());
                    vo.setHttpUrl(p.getHttpUrl());
                    vo.setHttpHeaders(p.getHttpHeaders());
                    vo.setHttpMethod(p.getHttpMethod());
                    vo.setTimeout(p.getTimeout());
                    if (p.getMappings() != null) {
                        vo.setMappings(p.getMappings().stream().map(m -> HTTPProtocolVO.ProtocolMapping.builder()
                                .mappingType(m.getMappingType())
                                .parentPath(m.getParentPath())
                                .fieldName(m.getFieldName())
                                .mcpPath(m.getMcpPath())
                                .mcpType(m.getMcpType())
                                .mcpDesc(m.getMcpDesc())
                                .isRequired(m.getIsRequired())
                                .sortOrder(m.getSortOrder())
                                .build()).collect(Collectors.toList()));
                    }
                    return vo;
                }).collect(Collectors.toList()));
            }
            adminProtocolService.saveGatewayProtocol(commandEntity);
            log.info("保存网关协议配置完成");
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (Exception e) {
            log.error("保存网关协议配置失败", e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "save_gateway_auth", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> saveGatewayAuth(@RequestBody GatewayConfigRequestDTO.GatewayAuth requestDTO) {
        try {
            log.info("保存网关auth认证开始 gatewayId: {}", requestDTO.getGatewayId());
            RegisterCommandEntity commandEntity = RegisterCommandEntity.builder()
                    .gatewayId(requestDTO.getGatewayId())
                    .rateLimit(requestDTO.getRateLimit())
                    .expireTime(requestDTO.getExpireTime())
                    .build();
            adminAuthService.saveGatewayAuth(commandEntity);
            log.info("保存网关auth认证完成 gatewayId: {}", requestDTO.getGatewayId());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (Exception e) {
            log.error("保存网关auth认证失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_config_list", method = RequestMethod.GET)
    @Override
    public Response<List<GatewayConfigDTO>> queryGatewayConfigList() {
        try {
            log.info("查询网关配置列表开始");
            List<GatewayConfigEntity> entities = adminManageService.queryGatewayConfigList();
            List<GatewayConfigDTO> dtoList = entities.stream().map(e -> GatewayConfigDTO.builder()
                    .gatewayId(e.getGatewayId())
                    .gatewayName(e.getGatewayName())
                    .gatewayDesc(e.getGatewayDesc())
                    .version(e.getVersion())
                    .auth(e.getAuth())
                    .status(e.getStatus())
                    .build()).collect(Collectors.toList());
            log.info("查询网关配置列表完成 count: {}", dtoList.size());
            return Response.<List<GatewayConfigDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .build();
        } catch (Exception e) {
            log.error("查询网关配置列表失败", e);
            return Response.<List<GatewayConfigDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
