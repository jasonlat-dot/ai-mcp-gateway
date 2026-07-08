package com.jasonlat.ai.trigger.http.admin;

import com.jasonlat.ai.cases.admin.IAdminGatewayAuthService;
import com.jasonlat.ai.cases.admin.IAdminManageService;
import com.jasonlat.ai.domain.admin.model.entity.GatewayAuthConfigEntity;
import com.jasonlat.ai.domain.admin.model.entity.GatewayAuthPageEntity;
import com.jasonlat.ai.domain.admin.model.entity.GatewayAuthQueryEntity;
import com.jasonlat.ai.domain.auth.model.entity.RegisterCommandEntity;
import com.jasonlat.ai.trigger.api.admin.IGatewayAuthAdminService;
import com.jasonlat.ai.trigger.api.dto.GatewayAuthDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayAuthQueryDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayConfigResponseDTO;
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
 * 网关认证管理
 *  - 路径前缀: /admin/auth/**
 *  - 负责: saveGatewayAuth、updateGatewayAuth、deleteGatewayAuth、queryGatewayAuthList/Page
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/admin/auth")
public class GatewayAuthController implements IGatewayAuthAdminService {

    @Resource
    private IAdminGatewayAuthService adminAuthService;

    @Resource
    private IAdminManageService adminManageService;

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
        } catch (AppException e) {
            log.warn("保存网关auth认证失败 gatewayId: {} code={} info={}", requestDTO.getGatewayId(), e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("保存网关auth认证失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "update_gateway_auth", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> updateGatewayAuth(@RequestBody GatewayConfigRequestDTO.GatewayAuth requestDTO) {
        try {
            log.info("修改网关auth认证开始 gatewayId: {}", requestDTO.getGatewayId());
            RegisterCommandEntity commandEntity = RegisterCommandEntity.builder()
                    .gatewayId(requestDTO.getGatewayId())
                    .rateLimit(requestDTO.getRateLimit())
                    .expireTime(requestDTO.getExpireTime())
                    .build();
            adminAuthService.updateGatewayAuth(commandEntity);
            log.info("修改网关auth认证完成 gatewayId: {}", requestDTO.getGatewayId());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (AppException e) {
            log.warn("修改网关auth认证失败 gatewayId: {} code={} info={}", requestDTO.getGatewayId(), e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("修改网关auth认证失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_auth_list", method = RequestMethod.GET)
    @Override
    public Response<List<GatewayAuthDTO>> queryGatewayAuthList() {
        try {
            log.info("查询网关认证列表开始");
            List<GatewayAuthConfigEntity> entities = adminManageService.queryGatewayAuthList();
            List<GatewayAuthDTO> dtoList = entities.stream().map(e -> GatewayAuthDTO.builder()
                    .gatewayId(e.getGatewayId())
                    .apiKey(e.getApiKey())
                    .rateLimit(e.getRateLimit())
                    .expireTime(e.getExpireTime())
                    .build()).collect(Collectors.toList());
            log.info("查询网关认证列表完成 count: {}", dtoList.size());
            return Response.<List<GatewayAuthDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .build();
        } catch (AppException e) {
            log.warn("查询网关认证列表失败 code={} info={}", e.getCode(), e.getInfo());
            return Response.<List<GatewayAuthDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询网关认证列表失败", e);
            return Response.<List<GatewayAuthDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_gateway_auth_page", method = RequestMethod.GET)
    @Override
    public ResponsePage<List<GatewayAuthDTO>> queryGatewayAuthPage(GatewayAuthQueryDTO queryDTO) {
        try {
            log.info("查询网关认证配置分页开始 gatewayId: {}, page: {}, rows: {}",
                    queryDTO.getGatewayId(), queryDTO.getPage(), queryDTO.getRows());

            GatewayAuthQueryEntity queryEntity = GatewayAuthQueryEntity.builder()
                    .gatewayId(queryDTO.getGatewayId())
                    .page(queryDTO.getPage() == null ? 1 : queryDTO.getPage())
                    .rows(queryDTO.getRows() == null ? 10 : queryDTO.getRows())
                    .build();

            GatewayAuthPageEntity pageEntity = adminManageService.queryGatewayAuthPage(queryEntity);
            List<GatewayAuthDTO> dtoList = pageEntity.getDataList().stream().map(e -> GatewayAuthDTO.builder()
                    .gatewayId(e.getGatewayId())
                    .apiKey(e.getApiKey())
                    .rateLimit(e.getRateLimit())
                    .expireTime(e.getExpireTime())
                    .build()).collect(Collectors.toList());
            log.info("查询网关认证配置分页完成 total: {}", pageEntity.getTotal());
            return ResponsePage.<List<GatewayAuthDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(dtoList)
                    .total(pageEntity.getTotal())
                    .build();
        } catch (AppException e) {
            log.warn("查询网关认证配置分页失败 code={} info={}", e.getCode(), e.getInfo());
            return ResponsePage.<List<GatewayAuthDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询网关认证配置分页失败", e);
            return ResponsePage.<List<GatewayAuthDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "delete_gateway_auth", method = RequestMethod.POST)
    @Override
    public Response<GatewayConfigResponseDTO> deleteGatewayAuth(@RequestParam String gatewayId) {
        try {
            log.info("删除网关认证配置开始 gatewayId: {}", gatewayId);
            adminAuthService.deleteGatewayAuth(gatewayId);
            log.info("删除网关认证配置完成 gatewayId: {}", gatewayId);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GatewayConfigResponseDTO.builder().success(true).build())
                    .build();
        } catch (AppException e) {
            log.warn("删除网关认证配置失败 gatewayId: {} code={} info={}", gatewayId, e.getCode(), e.getInfo());
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("删除网关认证配置失败 gatewayId: {}", gatewayId, e);
            return Response.<GatewayConfigResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
