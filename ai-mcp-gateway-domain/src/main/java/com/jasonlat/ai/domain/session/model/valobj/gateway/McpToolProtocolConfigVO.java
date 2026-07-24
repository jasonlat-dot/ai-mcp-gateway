package com.jasonlat.ai.domain.session.model.valobj.gateway;

import lombok.*;

import java.util.List;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpToolProtocolConfigVO {

    /**
     * 协议类型:HTTP / DUBBO
     */
    private String protocolType;

    /**
     * 请求协议配置
     */
    private HTTPConfig httpConfig;

    /**
     * Dubbo 工具配置(protocolType=DUBBO 时使用)
     */
    private DubboConfig dubboConfig;

    /**
     * 请求协议映射
     */
    private List<ProtocolMapping> requestProtocolMappings;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HTTPConfig {
        /**
         * 请求URL
         */
        private String url;
        /**
         * 请求头
         */
        private String headers;
        /**
         * 请求方法：POST/GET
         */
        private String method;
        /**
         * 请求超时时间（毫秒）
         */
        private Integer timeoutMs;
    }

    // ===================== Dubbo 配置(新增) =====================
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DubboConfig {
        /**
         * Provider 全限定接口名
         * 例:com.jasonlat.ai.dubbo.api.EmployeeService
         */
        private String interfaceName;
        /**
         * Dubbo 服务分组(对应 Provider @DubboService(group="..."))
         */
        private String group;
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
         * 方法参数类型全限定名列表(JSON 顺序与 LLM 参数顺序对齐)
         * 例:["com.jasonlat.ai.dubbo.api.dto.EmployeeRequest"]
         */
        private List<String> parameterTypes;
        /**
         * 单次调用超时(毫秒)
         */
        private Integer timeoutMs;
        /**
         * 失败重试次数(0=不重试)
         */
        private Integer retries;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProtocolMapping {
        /**
         * 映射类型：request-请求参数映射，response-响应数据映射
         */
        private String mappingType;
        /**
         * 父级路径（如：xxxRequest01，用于构建嵌套结构，根节点为NULL）
         */
        private String parentPath;
        /**
         * 字段名称（如：city、company、name）
         */
        private String fieldName;
        /**
         * MCP完整路径（如：xxxRequest01.city、xxxRequest01.company.name）
         */
        private String mcpPath;
        /**
         * MCP数据类型：string/number/boolean/object/array
         */
        private String mcpType;
        /**
         * MCP字段描述
         */
        private String mcpDesc;
        /**
         * 是否必填：0-否，1-是（用于生成required数组）
         */
        private Integer isRequired;
        /**
         * 排序顺序（同级字段排序）
         */
        private Integer sortOrder;
    }

}
