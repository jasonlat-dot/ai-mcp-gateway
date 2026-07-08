package com.jasonlat.ai.trigger.http.admin;

import com.jasonlat.ai.cases.admin.IAdminGatewayService;
import com.jasonlat.ai.cases.admin.IAdminManageService;
import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigEntity;
import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigPageEntity;
import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigQueryEntity;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.valobj.GatewayConfigVO;
import com.jasonlat.ai.trigger.api.admin.IGatewayAdminService;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigQueryDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigResponseDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.trigger.api.model.ResponsePage;
import com.jasonlat.ai.types.enums.GatewayEnum;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 网关基础配置管理
 *  - 路径前缀: /admin/gateway/**
 *  - 负责: saveGatewayConfig、updateGatewayConfig、queryGatewayConfigList/Page
 *  - 实现 IGatewayAdminService 接口契约(已按 controller 维度拆分)
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/admin/gateway")
public class GatewayController implements IGatewayAdminService {

    @Resource
    private IAdminGatewayService adminGatewayService;

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
        } catch (AppException e) {
            log.warn("保存网关配置失败 gatewayId: {} code={} info={}", requestDTO.getGatewayId(), e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("保存网关配置失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "update_gateway_config", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> updateGatewayConfig(@RequestBody GatewayConfigRequestDTO.GatewayConfig requestDTO) {
        try {
            log.info("修改网关配置开始 gatewayId: {}", requestDTO.getGatewayId());
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
            adminGatewayService.updateGatewayConfig(commandEntity);
            log.info("修改网关配置完成 gatewayId: {}", requestDTO.getGatewayId());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (AppException e) {
            log.warn("修改网关配置失败 gatewayId: {} code={} info={}", requestDTO.getGatewayId(), e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("修改网关配置失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "delete_gateway_config", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> deleteGatewayConfig(@RequestParam("gatewayId") String gatewayId) {
        try {
            log.info("删除网关配置开始 gatewayId: {}", gatewayId);
            boolean ok = adminGatewayService.deleteGatewayConfig(gatewayId);
            log.info("删除网关配置完成 gatewayId: {} ok={}", gatewayId, ok);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(ok).build())
                    .build();
        } catch (AppException e) {
            log.warn("删除网关配置失败 gatewayId: {} code={} info={}", gatewayId, e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("删除网关配置失败 gatewayId: {}", gatewayId, e);
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
        } catch (AppException e) {
            log.warn("查询网关配置列表失败 code={} info={}", e.getCode(), e.getInfo());
            return Response.<List<GatewayConfigDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询网关配置列表失败", e);
            return Response.<List<GatewayConfigDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_config_page", method = RequestMethod.GET)
    @Override
    public ResponsePage<List<GatewayConfigDTO>> queryGatewayConfigPage(GatewayConfigQueryDTO queryDTO) {
        try {
            log.info("查询网关配置分页开始 gatewayId: {}, gatewayName: {}, page: {}, rows: {}",
                    queryDTO.getGatewayId(), queryDTO.getGatewayName(), queryDTO.getPage(), queryDTO.getRows());

            GatewayConfigQueryEntity queryEntity = GatewayConfigQueryEntity.builder()
                    .gatewayId(queryDTO.getGatewayId())
                    .gatewayName(queryDTO.getGatewayName())
                    .page(queryDTO.getPage() == null ? 1 : queryDTO.getPage())
                    .rows(queryDTO.getRows() == null ? 10 : queryDTO.getRows())
                    .build();

            GatewayConfigPageEntity pageEntity = adminManageService.queryGatewayConfigPage(queryEntity);
            List<GatewayConfigDTO> dtoList = pageEntity.getDataList().stream().map(e -> GatewayConfigDTO.builder()
                    .gatewayId(e.getGatewayId())
                    .gatewayName(e.getGatewayName())
                    .gatewayDesc(e.getGatewayDesc())
                    .version(e.getVersion())
                    .auth(e.getAuth())
                    .status(e.getStatus())
                    .build()).collect(Collectors.toList());
            log.info("查询网关配置分页完成 total: {}", pageEntity.getTotal());
            return ResponsePage.<List<GatewayConfigDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .total(pageEntity.getTotal())
                    .build();
        } catch (AppException e) {
            log.warn("查询网关配置分页失败 code={} info={}", e.getCode(), e.getInfo());
            return ResponsePage.<List<GatewayConfigDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询网关配置分页失败", e);
            return ResponsePage.<List<GatewayConfigDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
