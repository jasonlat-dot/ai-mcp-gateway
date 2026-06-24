package com.jasonlat.ai.infrastructure.adapter.port;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonlat.ai.domain.session.adapter.port.ISessionPort;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.infrastructure.gateway.GenericHttpGateway;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jasonlat
 * 2026-06-24  18:59
 */
@Component
@AllArgsConstructor
public class SessionPort implements ISessionPort {

    private final GenericHttpGateway gateway;

    private final ObjectMapper objectMapper;

    @Override
    public Object toolCall(McpToolProtocolConfigVO.HTTPConfig httpConfig, Object params) throws IOException {

        // params 是map 不是就抛异常  --> {"word":"jsaonlat"}  key-value 形式
        if (!(params instanceof Map<?,?> arguments)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }

        String httpHeadersJson = httpConfig.getHeaders();
        Map<String, Object> headers = objectMapper.readValue(httpHeadersJson, Map.class);

        String httpMethod = httpConfig.getMethod().toLowerCase();

        switch (httpMethod) {
            case "post": {
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
            case "get": {
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
        throw new AppException(ResponseCode.METHOD_NOT_FOUND);
    }
}
