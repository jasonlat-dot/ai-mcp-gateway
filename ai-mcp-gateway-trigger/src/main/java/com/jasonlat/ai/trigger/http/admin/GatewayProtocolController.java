package com.jasonlat.ai.trigger.http.admin;

import com.jasonlat.ai.cases.admin.IAdminManageService;
import com.jasonlat.ai.cases.admin.IAdminProtocolService;
import com.jasonlat.ai.domain.admin.model.entity.GatewayProtocolConfigEntity;
import com.jasonlat.ai.domain.protocol.model.entity.AnalysisCommandEntity;
import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.trigger.api.admin.IGatewayProtocolAdminService;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigResponseDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayProtocolDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayProtocolQueryDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.trigger.api.model.ResponsePage;
import com.jasonlat.ai.domain.admin.model.entity.GatewayProtocolQueryEntity;
import com.jasonlat.ai.domain.admin.model.entity.GatewayProtocolPageEntity;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 网关协议配置管理
 *  - 路径前缀: /admin/protocol/**
 *  - 负责: saveGatewayProtocol、updateGatewayProtocol、deleteGatewayProtocol、importGatewayProtocol、analysisProtocol
 *         + queryGatewayProtocolList/Page/listByGatewayId
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/admin/protocol")
public class GatewayProtocolController implements IGatewayProtocolAdminService {

    @Resource
    private IAdminProtocolService adminProtocolService;

    @Resource
    private IAdminManageService adminManageService;

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
        } catch (AppException e) {
            log.warn("保存网关协议配置失败 code={} info={}", e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("保存网关协议配置失败", e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "update_gateway_protocol", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> updateGatewayProtocol(@RequestBody GatewayConfigRequestDTO.GatewayProtocol requestDTO) {
        try {
            log.info("修改网关协议配置开始");
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
            adminProtocolService.updateGatewayProtocol(commandEntity);
            log.info("修改网关协议配置完成");
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (AppException e) {
            log.warn("修改网关协议配置失败 code={} info={}", e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("修改网关协议配置失败", e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "import_gateway_protocol", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> importGatewayProtocol(@RequestBody GatewayConfigRequestDTO.GatewayProtocolImport requestDTO) {
        try {
            log.info("导入网关协议配置开始");
            AnalysisCommandEntity commandEntity = AnalysisCommandEntity.builder()
                    .openApiJson(requestDTO.getOpenApiJson())
                    .endpoints(requestDTO.getEndpoints())
                    .build();
            adminProtocolService.importGatewayProtocol(commandEntity);
            log.info("导入网关协议配置完成");
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (AppException e) {
            log.warn("导入网关协议配置失败 code={} info={}", e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("导入网关协议配置失败", e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "analysis_protocol", method = RequestMethod.POST)
    @Override
    public Response<List<GatewayProtocolDTO>> analysisProtocol(@RequestBody GatewayConfigRequestDTO.GatewayProtocolImport requestDTO) {
        try {
            log.info("解析网关协议配置开始");
            AnalysisCommandEntity commandEntity = AnalysisCommandEntity.builder()
                    .openApiJson(requestDTO.getOpenApiJson())
                    .endpoints(requestDTO.getEndpoints())
                    .build();
            List<HTTPProtocolVO> httpProtocolVOS = adminProtocolService.analysisProtocol(commandEntity);

            List<GatewayProtocolDTO> dtoList = httpProtocolVOS.stream().map(e -> GatewayProtocolDTO.builder()
                    .httpUrl(e.getHttpUrl())
                    .httpMethod(e.getHttpMethod())
                    .httpHeaders(e.getHttpHeaders())
                    .timeout(e.getTimeout())
                    .mappings(e.getMappings() == null ? null : e.getMappings().stream().map(m -> GatewayProtocolDTO.ProtocolMappingDTO.builder()
                            .mappingType(m.getMappingType())
                            .parentPath(m.getParentPath())
                            .fieldName(m.getFieldName())
                            .mcpPath(m.getMcpPath())
                            .mcpType(m.getMcpType())
                            .mcpDesc(m.getMcpDesc())
                            .isRequired(m.getIsRequired())
                            .build()).collect(Collectors.toList()))
                    .build()).collect(Collectors.toList());

            log.info("解析网关协议配置完成 size: {}", dtoList.size());
            return Response.<List<GatewayProtocolDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .build();
        } catch (AppException e) {
            log.warn("解析网关协议配置失败 code={} info={}", e.getCode(), e.getInfo());
            return Response.<List<GatewayProtocolDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("解析网关协议配置失败", e);
            return Response.<List<GatewayProtocolDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_protocol_list", method = RequestMethod.GET)
    public Response<List<GatewayProtocolDTO>> queryGatewayProtocolList() {
        try {
            log.info("查询网关协议列表开始");
            List<GatewayProtocolConfigEntity> entities = adminManageService.queryGatewayProtocolList();
            List<GatewayProtocolDTO> dtoList = entities.stream().map(e -> GatewayProtocolDTO.builder()
                    .protocolId(e.getProtocolId())
                    .httpUrl(e.getHttpUrl())
                    .httpMethod(e.getHttpMethod())
                    .httpHeaders(e.getHttpHeaders())
                    .timeout(e.getTimeout())
                    .mappings(e.getMappings() == null ? null : e.getMappings().stream().map(m -> GatewayProtocolDTO.ProtocolMappingDTO.builder()
                            .mappingType(m.getMappingType())
                            .parentPath(m.getParentPath())
                            .fieldName(m.getFieldName())
                            .mcpPath(m.getMcpPath())
                            .mcpType(m.getMcpType())
                            .mcpDesc(m.getMcpDesc())
                            .isRequired(m.getIsRequired())
                            .sortOrder(m.getSortOrder())
                            .build()).collect(Collectors.toList()))
                    .build()).collect(Collectors.toList());
            log.info("查询网关协议列表完成 count: {}", dtoList.size());
            return Response.<List<GatewayProtocolDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .build();
        } catch (AppException e) {
            log.warn("查询网关协议列表失败 code={} info={}", e.getCode(), e.getInfo());
            return Response.<List<GatewayProtocolDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询网关协议列表失败", e);
            return Response.<List<GatewayProtocolDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_protocol_page", method = RequestMethod.GET)
    @Override
    public ResponsePage<List<GatewayProtocolDTO>> queryGatewayProtocolPage(GatewayProtocolQueryDTO queryDTO) {
        try {
            log.info("查询网关协议分页开始 protocolId: {}, httpUrl: {}, page: {}, rows: {}",
                    queryDTO.getProtocolId(), queryDTO.getHttpUrl(), queryDTO.getPage(), queryDTO.getRows());

            GatewayProtocolQueryEntity queryEntity = GatewayProtocolQueryEntity.builder()
                    .protocolId(queryDTO.getProtocolId())
                    .httpUrl(queryDTO.getHttpUrl())
                    .page(queryDTO.getPage() == null ? 1 : queryDTO.getPage())
                    .rows(queryDTO.getRows() == null ? 10 : queryDTO.getRows())
                    .build();

            GatewayProtocolPageEntity pageEntity = adminManageService.queryGatewayProtocolPage(queryEntity);
            List<GatewayProtocolDTO> dtoList = pageEntity.getDataList().stream().map(e -> GatewayProtocolDTO.builder()
                    .protocolId(e.getProtocolId())
                    .httpUrl(e.getHttpUrl())
                    .httpMethod(e.getHttpMethod())
                    .httpHeaders(e.getHttpHeaders())
                    .timeout(e.getTimeout())
                    .mappings(e.getMappings() == null ? null : e.getMappings().stream().map(m -> GatewayProtocolDTO.ProtocolMappingDTO.builder()
                            .mappingType(m.getMappingType())
                            .parentPath(m.getParentPath())
                            .fieldName(m.getFieldName())
                            .mcpPath(m.getMcpPath())
                            .mcpType(m.getMcpType())
                            .mcpDesc(m.getMcpDesc())
                            .isRequired(m.getIsRequired())
                            .sortOrder(m.getSortOrder())
                            .build()).collect(Collectors.toList()))
                    .build()).collect(Collectors.toList());
            log.info("查询网关协议分页完成 total: {}", pageEntity.getTotal());
            return ResponsePage.<List<GatewayProtocolDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .total(pageEntity.getTotal())
                    .build();
        } catch (AppException e) {
            log.warn("查询网关协议分页失败 code={} info={}", e.getCode(), e.getInfo());
            return ResponsePage.<List<GatewayProtocolDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询网关协议分页失败", e);
            return ResponsePage.<List<GatewayProtocolDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_protocol_list_by_gateway_id", method = RequestMethod.GET)
    @Override
    public Response<List<GatewayProtocolDTO>> queryGatewayProtocolListByGatewayId(@RequestParam String gatewayId) {
        try {
            log.info("根据网关ID查询网关协议列表开始 gatewayId: {}", gatewayId);
            List<GatewayProtocolConfigEntity> entities = adminManageService.queryGatewayProtocolListByGatewayId(gatewayId);
            List<GatewayProtocolDTO> dtoList = entities.stream().map(e -> GatewayProtocolDTO.builder()
                    .protocolId(e.getProtocolId())
                    .httpUrl(e.getHttpUrl())
                    .httpMethod(e.getHttpMethod())
                    .httpHeaders(e.getHttpHeaders())
                    .timeout(e.getTimeout())
                    .mappings(e.getMappings() == null ? null : e.getMappings().stream().map(m -> GatewayProtocolDTO.ProtocolMappingDTO.builder()
                            .mappingType(m.getMappingType())
                            .parentPath(m.getParentPath())
                            .fieldName(m.getFieldName())
                            .mcpPath(m.getMcpPath())
                            .mcpType(m.getMcpType())
                            .mcpDesc(m.getMcpDesc())
                            .isRequired(m.getIsRequired())
                            .sortOrder(m.getSortOrder())
                            .build()).collect(Collectors.toList()))
                    .build()).collect(Collectors.toList());
            log.info("根据网关ID查询网关协议列表完成 count: {}", dtoList.size());
            return Response.<List<GatewayProtocolDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .build();
        } catch (AppException e) {
            log.warn("根据网关ID查询网关协议列表失败 gatewayId: {} code={} info={}", gatewayId, e.getCode(), e.getInfo());
            return Response.<List<GatewayProtocolDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("根据网关ID查询网关协议列表失败 gatewayId: {}", gatewayId, e);
            return Response.<List<GatewayProtocolDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "delete_gateway_protocol", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> deleteGatewayProtocol(@RequestParam Long protocolId) {
        try {
            log.info("删除网关协议配置开始 protocolId: {}", protocolId);
            adminProtocolService.deleteGatewayProtocol(protocolId);
            log.info("删除网关协议配置完成 protocolId: {}", protocolId);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (AppException e) {
            log.warn("删除网关协议配置失败 protocolId: {} code={} info={}", protocolId, e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("删除网关协议配置失败 protocolId: {}", protocolId, e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
