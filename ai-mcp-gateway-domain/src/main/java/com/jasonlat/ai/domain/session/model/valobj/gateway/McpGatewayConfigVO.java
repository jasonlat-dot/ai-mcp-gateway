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
     * 工具ID
     */
    private Long toolId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具描述
     */
    private String toolDesc;

    /**
     * 工具版本
     */
    private String toolVersion;
}
