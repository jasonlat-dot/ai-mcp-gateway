package com.jasonlat.ai.domain.gateway.model.valobj;

import com.jasonlat.ai.types.enums.GatewayEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 网关配置值对象
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GatewayConfigVO {

    /**
     * 网关唯一标识
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

    /**
     * 新版本
     */
    private String newVersion;


    /**
     * 校验状态：0-不校验，1-强校验
     */
    private GatewayEnum.GatewayAuthStatusEnum auth;

    /**
     * 网关状态：0-禁用，1-启用
     */
    private GatewayEnum.GatewayStatus status;

}
