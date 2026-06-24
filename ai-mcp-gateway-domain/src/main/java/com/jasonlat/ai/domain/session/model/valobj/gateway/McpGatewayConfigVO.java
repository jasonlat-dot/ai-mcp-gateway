package com.jasonlat.ai.domain.session.model.valobj.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jasonlat
 * 2026-05-17  15:04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpGatewayConfigVO {
    /**
     * 网关ID
     */
    private String gatewayId;

    /**
     * 网关名称
     */
    private String gatewayName;

    /**
     * 网关描述
     */
    private String gatewayDesc;

    /**
     * 协议版本
     */
    private String version;
}
