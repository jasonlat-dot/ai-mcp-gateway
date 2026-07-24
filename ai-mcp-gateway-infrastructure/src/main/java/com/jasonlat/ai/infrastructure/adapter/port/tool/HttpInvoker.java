package com.jasonlat.ai.infrastructure.adapter.port.tool;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO.HTTPConfig;
import com.jasonlat.ai.infrastructure.gateway.GenericHttpGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP 协议工具调用器:封装 OkHttp,支持 POST/GET,GET 支持路径参数占位符 {xxx}。
 * 业务层只需传入 HTTPConfig + 参数 Map。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpInvoker {

    private final GenericHttpGateway gateway;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object invoke(HTTPConfig httpConfig, Object params) throws IOException {
        if (!(params instanceof Map<?, ?> raw)) {
            // 必须是 Map,否则参数结构错
            throw new IllegalArgumentException(
                "HttpInvoker params must be Map, got: " + (params == null ? "null" : params.getClass().getName()));
        }
        Map<String, Object> arguments = (Map<String, Object>) raw;
        Map<String, Object> headers = objectMapper.readValue(httpConfig.getHeaders(), Map.class);
        String method = httpConfig.getMethod().toLowerCase();

        return switch (method) {
            case "post" -> doPost(httpConfig, headers, arguments);
            case "get"  -> doGet(httpConfig, headers, arguments);
            default -> throw new IllegalArgumentException("unsupported method: " + method);
        };
    }

    private Object doPost(HTTPConfig httpConfig, Map<String, Object> headers, Map<String, Object> arguments) throws IOException {
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                JSON.toJSONString(arguments.values().toArray()[0])
        );
        Call<ResponseBody> call = gateway.post(httpConfig.getUrl(), headers, requestBody);
        try (ResponseBody responseBody = call.execute().body()) {
            if (responseBody == null) {
                return null;
            }
            return responseBody.string();
        }
    }

    private Object doGet(HTTPConfig httpConfig, Map<String, Object> headers, Map<String, Object> arguments) throws IOException {
        HashMap<String, Object> objMapRequest = new HashMap<>((Map<String, Object>) arguments.values().toArray()[0]);

        String url = httpConfig.getUrl();
        // 替换路径参数
        // 匹配字符串里形如 {xxx} 的占位符，并且把 xxx 提取到分组 1  http://api/{userId}/info/{orderNo} 匹配到两处：{userId}、{orderNo}
        Matcher matcher = Pattern.compile("\\{([^}]+)}").matcher(url);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (objMapRequest.containsKey(name)) {
                url = url.replace("{" + name + "}", String.valueOf(objMapRequest.get(name)));
                objMapRequest.remove(name);
            }
        }

        Call<ResponseBody> call = gateway.get(url, headers, objMapRequest);
        try (ResponseBody responseBody = call.execute().body()) {
            if (responseBody == null) {
                return null;
            }
            return responseBody.string();
        }
    }

}