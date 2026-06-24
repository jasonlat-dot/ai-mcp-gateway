package com.jasonlat.ai.domain.session.model.valobj.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jasonlat
 * 2026-06-24  18:54
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpGatewayProtocolConfigVO {

    private HTTPConfig httpConfig;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HTTPConfig {
        /**
         * HTTP 请求 URL
         */
        private String url;

        /**
         * HTTP 请求方法
         */
        private String method;


        /**
         * HTTP 请求头
         */
        private String headers;


        /**
         * HTTP 请求超时时间，单位毫秒
         */
        private Integer timeoutMs;
    }
}
