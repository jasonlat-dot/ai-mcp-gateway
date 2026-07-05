package com.jasonlat.ai.domain.admin.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关配置实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GatewayConfigEntity {

    private String gatewayId;
    private String gatewayName;
    private String gatewayDesc;
    private String version;
    private Integer auth;
    private Integer status;

}
