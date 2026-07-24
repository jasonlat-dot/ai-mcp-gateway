package com.jasonlat.ai.infrastructure.dao.po;

import com.jasonlat.ai.infrastructure.dao.po.base.BasePagePO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Dubbo 协议配置表 — 与 mcp_gateway_tool.protocol_id 一对一。
 * <p>
 * 与 mcp_protocol_http 平级,通过 protocol_id + protocol_type 区分调用。
 * 该表只描述"如何调用一个 Dubbo 服务",不含参数映射(参数映射走 mcp_protocol_mapping 复用)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpProtocolDubboPO extends BasePagePO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 协议ID(外键 -> mcp_gateway_tool.protocol_id)
     */
    private Long protocolId;

    /**
     * Provider 全限定接口名
     * 例:com.jasonlat.ai.dubbo.api.EmployeeService
     */
    private String interfaceName;

    /**
     * Dubbo 服务分组(对应 @DubboService(group="..."))
     */
    private String groupName;

    /**
     * Dubbo 服务版本(对应 @DubboService(version="..."))
     */
    private String version;

    /**
     * 要调用的方法名
     * 例:getCompanyEmployee
     */
    private String methodName;

    /**
     * 方法参数类型全限定名列表(JSON 数组)
     * 例:["com.jasonlat.ai.dubbo.api.dto.EmployeeRequest"]
     * <p>
     * 用 JSON 存,因为 List<String> 直接建表不利于跨数据库。
     * 运行时 DubboInvoker 反序列化后用于 GenericService.$invoke。
     */
    private String parameterTypes;

    /**
     * 单次调用超时(毫秒)
     */
    private Integer timeout;

    /**
     * 失败重试次数(0=不重试)
     */
    private Integer retryTimes;

    /**
     * 状态:0-禁用,1-启用
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
