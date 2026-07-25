package com.jasonlat.ai.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GatewayProtocolDTO {

    private Long protocolId;
    private String httpUrl;
    private String httpMethod;
    private String httpHeaders;
    private Integer timeout;
    private List<ProtocolMappingDTO> mappings;

    /**
     * Dubbo 协议列表 — 与 http 字段平行,前端按需渲染。
     * <p>
     * 一次 query 接口可能同时返回 HTTP + Dubbo 协议,也可能是单一种(取决于 service 实现)。
     */
    private List<DubboProtocolDTO> dubboProtocols;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProtocolMappingDTO {
        private String mappingType;
        private String parentPath;
        private String fieldName;
        private String mcpPath;
        private String mcpType;
        private String mcpDesc;
        private Integer isRequired;
        private Integer sortOrder;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DubboProtocolDTO {
        private Long protocolId;
        private String interfaceName;
        private String groupName;
        private String version;
        private String methodName;
        private List<String> parameterTypes;
        private Integer timeout;
        private Integer retryTimes;
        private String directUrl;
        private Integer directEnabled;
        private Integer status;
        private List<ProtocolMappingDTO> mappings;
    }
}