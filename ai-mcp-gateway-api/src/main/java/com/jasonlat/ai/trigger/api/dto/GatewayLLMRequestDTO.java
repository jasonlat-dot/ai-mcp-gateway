package com.jasonlat.ai.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型请求测试
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayLLMRequestDTO {

    /**
     * 网关ID
     */
    private String gatewayId;

    /**
     * 认证Key
     */
    private String authApiKey;

    /**
     * 超时时间
     */
    private Integer timeout;

    /**
     * 请求信息
     */
    private String message;

}
