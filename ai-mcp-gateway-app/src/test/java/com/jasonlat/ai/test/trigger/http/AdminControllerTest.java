package com.jasonlat.ai.test.trigger.http;

import com.alibaba.fastjson.JSON;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMRequestDTO;
import com.jasonlat.ai.trigger.api.dto.GatewayLLMResponseDTO;
import com.jasonlat.ai.trigger.api.model.Response;
import com.jasonlat.ai.trigger.http.admin.GatewayLLMController;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AdminControllerTest {

    @Resource
    private GatewayLLMController llmController;

    @Test
    public void test_testCallGateway() {
        GatewayLLMRequestDTO requestDTO = GatewayLLMRequestDTO.builder()
                .gatewayId("gateway_001")
                .reload(true)
                .authApiKey("gw-8mvSBT1k9ydEKw9ZN0OSLnp3qcSwj2mde8890FGKdrfNwFto")
                .timeout(3000)
                .message("""
                获取公司雇员信息，信息如下；
                城市；北京
                公司；谷歌
                雇员；小傅哥""")
                .build();

        Response<GatewayLLMResponseDTO> response = llmController.testCallGateway(requestDTO);

        log.info("测试结果:{}", JSON.toJSONString(response));
    }

}
