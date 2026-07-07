package com.jasonlat.ai.infrastructure.dao.po;

import com.jasonlat.ai.infrastructure.dao.po.base.BasePagePO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * MCP网关配置表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpGatewayPO extends BasePagePO {

    /**
     * 主键ID
     */
    private Long id;
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
     * 状态：0-不校验，1-强校验
     */
    private Integer auth;

    /**
     * 协议版本
     */
    private String version;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

}
