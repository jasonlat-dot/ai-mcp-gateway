package com.jasonlat.ai.domain.protocol.model.valobj.enums;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author jasonlat
 * 2026-07-02  18:58
 */
@Getter
public enum AnalysisTypeEnum {

    swagger
    ;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public enum SwaggerAnalysisAction {

        requestBodyAnalysis("requestBodyAnalyze", "请求参数body对象解析"),
        parametersAnalysis("parametersAnalysis", "请求参数path对象解析"),
        ;
        private String code;
        private String info;

        public static SwaggerAnalysisAction get(JSONObject operation) {

            JSONObject requestBody = operation.getJSONObject("requestBody");
            JSONArray parameters = operation.getJSONArray("parameters");

            if (null != requestBody) {
                return requestBodyAnalysis;
            }

            if (null != parameters) {
                return parametersAnalysis;
            }

            throw new AppException(ResponseCode.ENUM_NOT_FOUND.getCode(), ResponseCode.ENUM_NOT_FOUND.getInfo());
        }
    }
}