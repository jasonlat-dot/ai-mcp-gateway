package com.jasonlat.ai.domain.protocol.service.analysis.strategy;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;

import java.util.List;

/**
 * @author jasonlat
 * 2026-07-03  19:08
 */
public abstract class AbstractAnalysisStrategy implements IProtocolAnalysisStrategy {

    protected void parseProperties(String parentMcpPath, JSONObject properties, JSONArray requiredList, JSONObject definitions, List<HTTPProtocolVO.ProtocolMapping> mappings) {
        if (properties == null) return;

        int sortOrder = 1;
        for (String propName : properties.keySet()) {
            JSONObject prop = properties.getJSONObject(propName);

            // 拼接关系链 a.b.c
            String currentMcpPath = parentMcpPath + "." + propName;

            JSONObject effectiveSchema = prop;
            String type = prop.getString("type");
            String description = prop.getString("description");

            // 获取应用关系对象
            if (prop.containsKey("$ref")) {
                String ref = prop.getString("$ref");
                String refName = ref.substring(ref.lastIndexOf('/') + 1);
                effectiveSchema = definitions.getJSONObject(refName);
                if (type == null) type = effectiveSchema.getString("type");
                if (description == null) description = effectiveSchema.getString("description");
            }

            HTTPProtocolVO.ProtocolMapping mapping = HTTPProtocolVO.ProtocolMapping.builder()
                    .mappingType("request")
                    .parentPath(parentMcpPath)
                    .fieldName(propName)
                    .mcpPath(currentMcpPath)
                    .mcpType(convertType(type))
                    .mcpDesc(description)
                    .isRequired(requiredList != null && requiredList.contains(propName) ? 1 : 0)
                    .sortOrder(sortOrder++)
                    .build();
            mappings.add(mapping);

            // 如果存在下一个引用对象，则嵌套循环继续寻找
            if (effectiveSchema.containsKey("properties")) {
                parseProperties(currentMcpPath, effectiveSchema.getJSONObject("properties"), effectiveSchema.getJSONArray("required"), definitions, mappings);
            }
        }
    }

    // MCP数据类型：string/number/boolean/object/array
    protected String convertType(String type) {
        if (type == null) return "string";
        return switch (type.toLowerCase()) {
            case "string", "char", "date", "datetime" -> "string";
            case "integer", "int", "long", "double", "float", "number" -> "number";
            case "boolean", "bool" -> "boolean";
            case "array", "list" -> "array";
            default -> "object";
        };
    }

    protected String toLowerCamel(String name) {
        if (name == null || name.isEmpty()) return name;
        char[] cs = name.toCharArray();
        cs[0] = Character.toLowerCase(cs[0]);
        return new String(cs);
    }
}
