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
     * 直连 URL 列表,英文逗号分隔,优先级最高。
     * 格式:dubbo://host:port 或 tri://host:port;多个 URL 用 "," 分隔,不允许多余空格(由 Repository 解析时 trim)。
     * 例:"dubbo://10.0.0.5:20880,dubbo://10.0.0.6:20880"
     * 设置后绕过 Nacos 服务发现,按顺序故障转移(首个失败切下一个)。
     * 主要用于本地调试、Provider 未注册到 Nacos、灰度指定实例等场景。
     * <p>
     * 存储为什么不用 List / JSON / text[]:
     * 1) 测试场景下运维手填,逗号分隔最直观,SQL 客户端可直接看。
     * 2) 字段出现频率低,集中解析发生在 Repository 一次,不会成为性能瓶颈。
     * 3) 避免了 JSON 字段、引号转义、PostgreSQL 数组驱动兼容性问题。
     * <p>
     * 业务层 VO 的 {@code directUrls} 字段是 List<String>,由 Repository 拆分得到。
     */
    private String directUrl;

    /**
     * 是否启用直连:true=按 directUrls 顺序故障转移调用,false=走 Nacos 默认发现。
     * 留这个开关的目的是:Nacos 配置可以保留,只在调试时切换;
     * 不留开关的话,留空字符串就是关闭,但运维/调试时容易忘记清空导致误连。
     * <p>
     * 即便 directEnabled=true,directUrl 为空时也应安全回退到 Nacos(由 Invoker 层判断)。
     */
    private Integer directEnabled;

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
