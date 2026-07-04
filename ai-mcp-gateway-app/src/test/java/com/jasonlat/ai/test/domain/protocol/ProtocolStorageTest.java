package com.jasonlat.ai.test.domain.protocol;

import com.alibaba.fastjson.JSON;
import com.jasonlat.ai.domain.protocol.model.entity.AnalysisCommandEntity;
import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.domain.protocol.service.IProtocolAnalysisService;
import com.jasonlat.ai.domain.protocol.service.IProtocolStorageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProtocolStorageTest {

    @Value("classpath:swagger/api-docs-test.json")
    private org.springframework.core.io.Resource apiDocs;

    @Autowired
    private IProtocolAnalysisService protocolAnalysis;

    @Resource
    private IProtocolStorageService protocolStorage;

    @Test
    public void test_storage() throws IOException {
        // 1. 协议解析
        String json = new String(FileCopyUtils.copyToByteArray(apiDocs.getInputStream()), StandardCharsets.UTF_8);
        List<String> endpoints = Arrays.asList("/api/v1/mcp/get_company_employee");
//        List<String> endpoints = Arrays.asList("/api/v1/mcp/query-test03");
//        List<String> endpoints = Arrays.asList("/api/v1/mcp/query-test02");
//        List<String> endpoints = Arrays.asList("/api/v1/mcp/query-by-id-01");
//        List<String> endpoints = Arrays.asList("/api/v1/mcp/query-by-id-02");
//        List<String> endpoints = Arrays.asList("/api/v1/mcp/query-by-id-03");

        AnalysisCommandEntity commandEntity = AnalysisCommandEntity.builder()
                .openApiJson(json)
                .endpoints(endpoints)
                .build();

        List<HTTPProtocolVO> httpProtocolVOS = protocolAnalysis.analysis(commandEntity);
        log.info("解析协议:{}", JSON.toJSONString(httpProtocolVOS));

        // 2. 协议存储
        List<Long> protocolIdList = protocolStorage.storage(
                StorageCommandEntity.builder()
                        .httpProtocolVOS(httpProtocolVOS).build());

        log.info("存储协议:{}", JSON.toJSONString(protocolIdList));
    }

}
