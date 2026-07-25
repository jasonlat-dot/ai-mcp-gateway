package com.jasonlat.ai.domain.protocol.service.analysis.http.strategy.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.domain.protocol.service.analysis.http.strategy.AbstractAnalysisStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jasonlat
 * 2026-07-03  19:06
 */
@Service("parametersAnalysis")
public class ParametersAnalysisStrategy extends AbstractAnalysisStrategy {
    /**
     * 执行解析
     *
     * @param operation   操作
     * @param definitions 定义
     * @param mappings    映射
     */
    @Override
    public void doAnalysis(JSONObject operation, JSONObject definitions, List<HTTPProtocolVO.ProtocolMapping> mappings) {
        // 2. Parse parameters - 属性入参
        JSONArray parameters = operation.getJSONArray("parameters");
        if (parameters == null || parameters.isEmpty()) return;

        for (int i = 0; i < parameters.size(); i++) {
            JSONObject param = parameters.getJSONObject(i);
            String in = param.getString("in");
            if (!"query".equals(in) && !"path".equals(in)) continue;

            String name = param.getString("name");
            boolean required = param.getBooleanValue("required");
            String description = param.getString("description");

            JSONObject schema = param.getJSONObject("schema");
            String type = schema.getString("type");
            String ref = schema.getString("$ref");

            if (ref != null) {
                String refName = ref.substring(ref.lastIndexOf('/') + 1);
                JSONObject reqSchema = definitions.getJSONObject(refName);

                if (type == null) type = reqSchema.getString("type");
                if (description == null) description = reqSchema.getString("description");

                HTTPProtocolVO.ProtocolMapping rootMapping = HTTPProtocolVO.ProtocolMapping.builder()
                        .mappingType("request")
                        .parentPath(null)
                        .fieldName(name)
                        .mcpPath(name)
                        .mcpType(convertType(type))
                        .mcpDesc(description)
                        .isRequired(required ? 1 : 0)
                        .sortOrder(mappings.size() + 1)
                        .build();
                mappings.add(rootMapping);

                parseProperties(name, reqSchema.getJSONObject("properties"), reqSchema.getJSONArray("required"), definitions, mappings);
            } else {
                HTTPProtocolVO.ProtocolMapping mapping = HTTPProtocolVO.ProtocolMapping.builder()
                        .mappingType("request")
                        .parentPath(null)
                        .fieldName(name)
                        .mcpPath(name)
                        .mcpType(convertType(type))
                        .mcpDesc(description)
                        .isRequired(required ? 1 : 0)
                        .sortOrder(mappings.size() + 1)
                        .build();
                mappings.add(mapping);
            }
        }
    }
}
