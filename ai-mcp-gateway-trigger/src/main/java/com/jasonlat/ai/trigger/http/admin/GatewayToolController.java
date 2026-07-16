package com.jasonlat.ai.trigger.http.admin;

import com.jasonlat.ai.cases.admin.IAdminGatewayService;
import com.jasonlat.ai.cases.admin.IAdminManageService;
import com.jasonlat.ai.domain.admin.model.entity.GatewayToolConfigEntity;
import com.jasonlat.ai.domain.admin.model.entity.GatewayToolPageEntity;
import com.jasonlat.ai.domain.admin.model.entity.GatewayToolQueryEntity;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayToolConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.valobj.GatewayToolConfigVO;
import com.jasonlat.ai.trigger.api.admin.IGatewayToolAdminService;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigResponseDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayToolConfigDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayToolQueryDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.trigger.api.model.ResponsePage;
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
 * 网关工具配置管理
 *  - 路径前缀: /admin/tool/**
 *  - 负责: saveGatewayToolConfig、updateGatewayToolConfig、deleteGatewayToolConfig、queryGatewayToolList/Page/listByGatewayId
 *  - 写操作复用 IAdminGatewayService(业务层 save/update 工具挂在该 service 上)
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/admin/tool")
public class GatewayToolController implements IGatewayToolAdminService {

    @Resource
    private IAdminGatewayService adminGatewayService;

    @Resource
    private IAdminManageService adminManageService;

    @RequestMapping(value = "save_gateway_tool_config", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> saveGatewayToolConfig(@RequestBody GatewayConfigRequestDTO.GatewayToolConfig requestDTO) {
        try {
            log.info("保存网关工具配置开始 gatewayId: {}", requestDTO.getGatewayId());
            GatewayToolConfigCommandEntity commandEntity = GatewayToolConfigCommandEntity.builder()
                    .gatewayToolConfigVO(GatewayToolConfigVO.builder()
                            .gatewayId(requestDTO.getGatewayId())
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
        } catch (AppException e) {
            log.warn("保存网关工具配置失败 gatewayId: {} code={} info={}", requestDTO.getGatewayId(), e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("保存网关工具配置失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "update_gateway_tool_config", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> updateGatewayToolConfig(@RequestBody GatewayConfigRequestDTO.GatewayToolConfig requestDTO) {
        try {
            log.info("修改网关工具配置开始 gatewayId: {} toolId: {}", requestDTO.getGatewayId(), requestDTO.getToolId());
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
            adminGatewayService.updateGatewayToolConfig(commandEntity);
            log.info("修改网关工具配置完成 gatewayId: {} toolId: {}", requestDTO.getGatewayId(), requestDTO.getToolId());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (AppException e) {
            log.warn("修改网关工具配置失败 gatewayId: {} toolId: {} code={} info={}", requestDTO.getGatewayId(), requestDTO.getToolId(), e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("修改网关工具配置失败 gatewayId: {} toolId: {}", requestDTO.getGatewayId(), requestDTO.getToolId(), e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_tool_list", method = RequestMethod.GET)
    @Override
    public Response<List<GatewayToolConfigDTO>> queryGatewayToolList() {
        try {
            log.info("查询网关工具列表开始");
            List<GatewayToolConfigEntity> entities = adminManageService.queryGatewayToolList();
            List<GatewayToolConfigDTO> dtoList = entities.stream().map(e -> GatewayToolConfigDTO.builder()
                    .gatewayId(e.getGatewayId())
                    .toolId(e.getToolId())
                    .toolName(e.getToolName())
                    .toolType(e.getToolType())
                    .toolDescription(e.getToolDescription())
                    .toolVersion(e.getToolVersion())
                    .protocolId(e.getProtocolId())
                    .protocolType(e.getProtocolType())
                    .build()).collect(Collectors.toList());
            log.info("查询网关工具列表完成 count: {}", dtoList.size());
            return Response.<List<GatewayToolConfigDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .build();
        } catch (AppException e) {
            log.warn("查询网关工具列表失败 code={} info={}", e.getCode(), e.getInfo());
            return Response.<List<GatewayToolConfigDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询网关工具列表失败", e);
            return Response.<List<GatewayToolConfigDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_tool_page", method = RequestMethod.GET)
    @Override
    public ResponsePage<List<GatewayToolConfigDTO>> queryGatewayToolPage(GatewayToolQueryDTO queryDTO) {
        try {
            log.info("查询网关工具分页开始 gatewayId: {}, toolId: {}, page: {}, rows: {}",
                    queryDTO.getGatewayId(), queryDTO.getToolId(), queryDTO.getPage(), queryDTO.getRows());

            GatewayToolQueryEntity queryEntity = GatewayToolQueryEntity.builder()
                    .gatewayId(queryDTO.getGatewayId())
                    .toolId(queryDTO.getToolId())
                    .page(queryDTO.getPage() == null ? 1 : queryDTO.getPage())
                    .rows(queryDTO.getRows() == null ? 10 : queryDTO.getRows())
                    .build();

            GatewayToolPageEntity pageEntity = adminManageService.queryGatewayToolPage(queryEntity);
            List<GatewayToolConfigDTO> dtoList = pageEntity.getDataList().stream().map(e -> GatewayToolConfigDTO.builder()
                    .gatewayId(e.getGatewayId())
                    .toolId(e.getToolId())
                    .toolName(e.getToolName())
                    .toolType(e.getToolType())
                    .toolDescription(e.getToolDescription())
                    .toolVersion(e.getToolVersion())
                    .protocolId(e.getProtocolId())
                    .protocolType(e.getProtocolType())
                    .build()).collect(Collectors.toList());
            log.info("查询网关工具分页完成 total: {}", pageEntity.getTotal());
            return ResponsePage.<List<GatewayToolConfigDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .total(pageEntity.getTotal())
                    .build();
        } catch (AppException e) {
            log.warn("查询网关工具分页失败 code={} info={}", e.getCode(), e.getInfo());
            return ResponsePage.<List<GatewayToolConfigDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询网关工具分页失败", e);
            return ResponsePage.<List<GatewayToolConfigDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_tool_list_by_gateway_id", method = RequestMethod.GET)
    @Override
    public Response<List<GatewayToolConfigDTO>> queryGatewayToolListByGatewayId(@RequestParam("gatewayId") String gatewayId) {
        try {
            log.info("根据网关ID查询网关工具列表开始 gatewayId: {}", gatewayId);
            List<GatewayToolConfigEntity> entities = adminManageService.queryGatewayToolListByGatewayId(gatewayId);
            List<GatewayToolConfigDTO> dtoList = entities.stream().map(e -> GatewayToolConfigDTO.builder()
                    .gatewayId(e.getGatewayId())
                    .toolId(e.getToolId())
                    .toolName(e.getToolName())
                    .toolType(e.getToolType())
                    .toolDescription(e.getToolDescription())
                    .toolVersion(e.getToolVersion())
                    .protocolId(e.getProtocolId())
                    .protocolType(e.getProtocolType())
                    .build()).collect(Collectors.toList());
            log.info("根据网关ID查询网关工具列表完成 count: {}", dtoList.size());
            return Response.<List<GatewayToolConfigDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .build();
        } catch (AppException e) {
            log.warn("根据网关ID查询网关工具列表失败 gatewayId: {} code={} info={}", gatewayId, e.getCode(), e.getInfo());
            return Response.<List<GatewayToolConfigDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("根据网关ID查询网关工具列表失败 gatewayId: {}", gatewayId, e);
            return Response.<List<GatewayToolConfigDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "delete_gateway_tool_config", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> deleteGatewayToolConfig(@RequestParam("gatewayId") String gatewayId,
                                                                       @RequestParam("toolId") Long toolId) {
        try {
            log.info("删除网关工具配置开始 gatewayId: {} toolId: {}", gatewayId, toolId);
            adminGatewayService.deleteGatewayToolConfig(toolId);
            log.info("删除网关工具配置完成 gatewayId: {} toolId: {}", gatewayId, toolId);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (AppException e) {
            log.warn("删除网关工具配置失败 gatewayId: {} toolId: {} code={} info={}", gatewayId, toolId, e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("删除网关工具配置失败 gatewayId: {} toolId: {}", gatewayId, toolId, e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
