package com.jasonlat.ai.domain.protocol.service.analysis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jasonlat.ai.domain.protocol.model.entity.AnalysisCommandEntity;
import com.jasonlat.ai.domain.protocol.model.valobj.enums.AnalysisTypeEnum;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.domain.protocol.service.IProtocolAnalysisService;
import com.jasonlat.ai.domain.protocol.service.analysis.http.strategy.IProtocolAnalysisStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jasonlat
 * 2026-07-03  18:58
 */
@Slf4j
@Service
public class ProtocolAnalysisService implements IProtocolAnalysisService {

    private final Map<String, IProtocolAnalysisStrategy> protocolAnalysisStrategyMap;

    public ProtocolAnalysisService(Map<String, IProtocolAnalysisStrategy> strategyMap) {
        this.protocolAnalysisStrategyMap = strategyMap;
    }

    /**
     * 解析HTTP协议
     *
     * @param commandEntity 解析命令
     * @return 解析结果
     */
    @Override
    public List<HTTPProtocolVO> analysis(AnalysisCommandEntity commandEntity) {
        log.info("协议解析请求 endpoints:{} openApiJson:{}", JSON.toJSONString(commandEntity.getEndpoints()), commandEntity.getOpenApiJson());

        List<HTTPProtocolVO> list = new ArrayList<>();
        try {
            JSONObject root = JSON.parseObject(commandEntity.getOpenApiJson());
            String baseUrl = root.getJSONArray("servers").getJSONObject(0).getString("url");
            JSONObject paths = root.getJSONObject("paths");
            JSONObject schemas = root.getJSONObject("components").getJSONObject("schemas");

            List<String> endpoints = commandEntity.getEndpoints();
            if (null == endpoints || endpoints.isEmpty()) return list;

            for (String endpoint : endpoints) {
                JSONObject pathItem = paths.getJSONObject(endpoint);
                if (pathItem == null) continue;

                String method = detectMethod(pathItem);
                JSONObject operation = pathItem.getJSONObject(method);

                HTTPProtocolVO httpProtocolVO = new HTTPProtocolVO();
                httpProtocolVO.setHttpUrl(baseUrl + endpoint);
                httpProtocolVO.setHttpMethod(method);
                httpProtocolVO.setHttpHeaders(JSON.toJSONString(new HashMap<>() {{
                    put("Content-Type", "application/json");
                }}));
                httpProtocolVO.setTimeout(30000);

                List<HTTPProtocolVO.ProtocolMapping> mappings = new ArrayList<>();

                // 枚举策略动作处理
                AnalysisTypeEnum.SwaggerAnalysisAction analysisAction = AnalysisTypeEnum.SwaggerAnalysisAction.get(operation);
                IProtocolAnalysisStrategy strategy = protocolAnalysisStrategyMap.get(analysisAction.getCode());
                strategy.doAnalysis(operation, schemas, mappings);

                httpProtocolVO.setMappings(mappings);
                list.add(httpProtocolVO);
            }

        } catch (Exception e) {
            log.error("协议解析失败 endpoints:{} openApiJson:{}", JSON.toJSONString(commandEntity.getEndpoints()), commandEntity.getOpenApiJson(), e);
        }

        return list;
    }

    private String detectMethod(JSONObject pathItem) {
        if (pathItem.containsKey("post")) return "post";
        if (pathItem.containsKey("get")) return "get";
        if (pathItem.containsKey("put")) return "put";
        if (pathItem.containsKey("delete")) return "delete";
        return "post";
    }
}
