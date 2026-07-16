package com.jasonlat.ai.trigger.http.admin;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.cases.admin.IAdminLLMService;
import com.jasonlat.ai.trigger.api.admin.IGatewayLlmService;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMResponseDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author jasonlat
 * 2026-07-15  19:20
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/admin/llm")
public class GatewayLLMController implements IGatewayLlmService {

    @Resource
    private IAdminLLMService adminLLMService;

    @Override
    @RequestMapping(value = "test_call_gateway", method = RequestMethod.POST)
    public Response<GatewayLLMResponseDTO> testCallGateway(@RequestBody GatewayLLMRequestDTO requestDTO) {
        try {
            log.info("测试请求网关服务开始 gatewayId: {}", requestDTO.getGatewayId());

            GatewayLLMResponseDTO responseDTO = adminLLMService.testCallGateway(requestDTO);
            log.info("测试请求网关服务完成 gatewayId: {} resDTO:{}", requestDTO.getGatewayId(), JSON.toJSONString(responseDTO));

            return Response.<GatewayLLMResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTO)
                    .build();
        }catch (AppException e) {
            log.error("测试请求网关服务失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayLLMResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("测试请求网关服务失败 gatewayId: {}", requestDTO.getGatewayId(), e);
            return Response.<GatewayLLMResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
