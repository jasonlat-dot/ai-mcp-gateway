package com.jasonlat.ai.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 网关配置信息 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GatewayConfigDTO implements Serializable {

    private String gatewayId;
    private String gatewayName;
    private String gatewayDesc;
    private String version;
    private Integer auth;
    private Integer status;

    /** 当前运行环境下的 MCP SSE 完整地址。 */
    private String sseUrl;

    /** 当前运行环境下的 MCP Streamable HTTP 完整地址。 */
    private String streamableUrl;

}
