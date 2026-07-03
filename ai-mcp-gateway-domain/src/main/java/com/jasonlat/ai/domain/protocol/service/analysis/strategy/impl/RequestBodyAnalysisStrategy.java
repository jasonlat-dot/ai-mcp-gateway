package com.jasonlat.ai.domain.protocol.service.analysis.strategy.impl;

import com.alibaba.fastjson2.JSONObject;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.domain.protocol.service.analysis.strategy.AbstractAnalysisStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jasonlat
 * 2026-07-03  19:07
 */
@Service("requestBodyAnalyze")
public class RequestBodyAnalysisStrategy extends AbstractAnalysisStrategy {
    /**
     * 执行解析
     *
     * @param operation   操作
     * @param definitions 定义
     * @param mappings    映射
     */
    @Override
    public void doAnalysis(JSONObject operation, JSONObject definitions, List<HTTPProtocolVO.ProtocolMapping> mappings) {
        // 1. Parse requestBody - 对象入参
        JSONObject requestBody = operation.getJSONObject("requestBody");
        if (requestBody == null) return;

        JSONObject content = requestBody.getJSONObject("content");
        JSONObject appJson = content.getJSONObject("application/json");
        if (appJson == null) return;

        JSONObject schema = appJson.getJSONObject("schema");
        if (schema == null) return;
        String ref = schema.getString("$ref");
        if (ref == null) return;

        String refName = ref.substring(ref.lastIndexOf('/') + 1);
        JSONObject reqSchema = definitions.getJSONObject(refName);
        String rootName = toLowerCamel(refName);

        HTTPProtocolVO.ProtocolMapping rootMapping = HTTPProtocolVO.ProtocolMapping.builder()
                .mappingType("request")
                .parentPath(null)
                .fieldName(rootName)
                .mcpPath(rootName)
                .mcpType(convertType(reqSchema.getString("type")))
                .mcpDesc(reqSchema.getString("description"))
                .isRequired(1)
                .sortOrder(1)
                .build();
        mappings.add(rootMapping);

        parseProperties(rootName, reqSchema.getJSONObject("properties"), reqSchema.getJSONArray("required"), definitions, mappings);
    }
}
