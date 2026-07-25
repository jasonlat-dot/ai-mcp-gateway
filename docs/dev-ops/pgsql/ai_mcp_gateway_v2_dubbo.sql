-- ************************************************************
-- PostgreSQL SQL dump
-- 数据库: ai_mcp_gateway_v2
-- 文件说明: 新增 Dubbo 协议配置表
-- 适用版本: v2 + (在 v2 上叠加)
-- 生成时间: 2026-07-24
-- ************************************************************

-- 转储表 mcp_protocol_dubbo
-- 跟 mcp_protocol_http 平级,通过 protocol_id 关联 mcp_gateway_tool
-- 每条 tool 只对应一张协议配置表(HTTP/DUBBO 二选一),由 mcp_gateway_tool.protocol_type 决定
-- ------------------------------------------------------------

DROP TABLE IF EXISTS mcp_protocol_dubbo CASCADE;

CREATE TABLE mcp_protocol_dubbo (
                                    id BIGSERIAL PRIMARY KEY,
                                    protocol_id BIGINT NOT NULL,
                                    interface_name VARCHAR(512) NOT NULL,
                                    group_name VARCHAR(128) DEFAULT '',
                                    version VARCHAR(64) DEFAULT '',
                                    method_name VARCHAR(256) NOT NULL,
                                    parameter_types TEXT,
                                    timeout INTEGER DEFAULT 3000,
                                    retry_times SMALLINT DEFAULT 0,
                                    direct_url VARCHAR(2048) DEFAULT NULL,
                                    direct_enabled SMALLINT NOT NULL DEFAULT 0,
                                    status SMALLINT NOT NULL DEFAULT 1,
                                    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  mcp_protocol_dubbo                       IS 'MCP Dubbo 协议配置表';
COMMENT ON COLUMN mcp_protocol_dubbo.id                    IS '主键ID';
COMMENT ON COLUMN mcp_protocol_dubbo.protocol_id           IS '协议ID(外键 -> mcp_gateway_tool.protocol_id)';
COMMENT ON COLUMN mcp_protocol_dubbo.interface_name        IS 'Provider 全限定接口名,例:com.jasonlat.ai.dubbo.api.EmployeeService';
COMMENT ON COLUMN mcp_protocol_dubbo.group_name            IS 'Dubbo 服务分组(对应 @DubboService(group=...))';
COMMENT ON COLUMN mcp_protocol_dubbo.version               IS 'Dubbo 服务版本(对应 @DubboService(version=...))';
COMMENT ON COLUMN mcp_protocol_dubbo.method_name           IS '要调用的方法名,例:getCompanyEmployee';
COMMENT ON COLUMN mcp_protocol_dubbo.parameter_types       IS '方法参数类型全限定名 JSON 数组,例:["com.xxx.dto.EmployeeRequest"]';
COMMENT ON COLUMN mcp_protocol_dubbo.timeout               IS '单次调用超时(毫秒)';
COMMENT ON COLUMN mcp_protocol_dubbo.retry_times           IS '失败重试次数(0=不重试)';
COMMENT ON COLUMN mcp_protocol_dubbo.direct_url            IS '直连 URL 列表,英文逗号分隔。例:"dubbo://10.0.0.5:20880,dubbo://10.0.0.6:20880"。空则走 Nacos';
COMMENT ON COLUMN mcp_protocol_dubbo.direct_enabled        IS '是否启用直连:0-否(走 Nacos),1-是(按 direct_url 顺序故障转移)';
COMMENT ON COLUMN mcp_protocol_dubbo.status                IS '状态:0-禁用,1-启用';
COMMENT ON COLUMN mcp_protocol_dubbo.create_time           IS '创建时间';
COMMENT ON COLUMN mcp_protocol_dubbo.update_time           IS '更新时间';

CREATE INDEX idx_dubbo_protocol_id ON mcp_protocol_dubbo(protocol_id);
CREATE INDEX idx_dubbo_status      ON mcp_protocol_dubbo(status);

-- 注意:此处不插入示例数据。
-- 真实使用流程:
--   1. 先在 mcp_gateway_tool 插入一条 protocol_type='DUBBO' 的记录,获得 protocol_id
--   2. 再在 mcp_protocol_dubbo 插入对应配置(同一 protocol_id)
--   3. 在 mcp_protocol_mapping 添加工具的请求/响应字段映射(复用现有表)
-- 例(可手动执行):
--   INSERT INTO mcp_protocol_dubbo (protocol_id, interface_name, group_name, version, method_name, parameter_types, timeout, status)
--   VALUES (2, 'com.jasonlat.ai.dubbo.api.EmployeeService', 'default', '1.0.0',
--           'getCompanyEmployee', '["com.jasonlat.ai.dubbo.api.dto.EmployeeRequest"]', 5000, 1);
