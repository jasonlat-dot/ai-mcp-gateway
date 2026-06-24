-- ************************************************************
-- PostgreSQL SQL dump
-- 数据库: ai_mcp_gateway_v2
-- 生成时间: 2026-02-01 11:57:07
-- ************************************************************

-- 创建数据库
CREATE DATABASE ai_mcp_gateway_v2
    WITH ENCODING 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

\c ai_mcp_gateway_v2;

-- 转储表 mcp_gateway
-- ------------------------------------------------------------

DROP TABLE IF EXISTS mcp_gateway CASCADE;

CREATE TABLE mcp_gateway (
                             id BIGSERIAL PRIMARY KEY,
                             gateway_id VARCHAR(64) NOT NULL UNIQUE,
                             gateway_name VARCHAR(128) NOT NULL,
                             gateway_desc VARCHAR(512),
                             version VARCHAR(16),
                             status SMALLINT NOT NULL DEFAULT 1,
                             create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mcp_gateway IS 'MCP网关配置表';
COMMENT ON COLUMN mcp_gateway.id IS '主键ID';
COMMENT ON COLUMN mcp_gateway.gateway_id IS '网关唯一标识';
COMMENT ON COLUMN mcp_gateway.gateway_name IS '网关名称';
COMMENT ON COLUMN mcp_gateway.gateway_desc IS '网关描述';
COMMENT ON COLUMN mcp_gateway.version IS '网关版本';
COMMENT ON COLUMN mcp_gateway.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN mcp_gateway.create_time IS '创建时间';
COMMENT ON COLUMN mcp_gateway.update_time IS '更新时间';

CREATE INDEX idx_status ON mcp_gateway(status);

INSERT INTO mcp_gateway (id, gateway_id, gateway_name, gateway_desc, version, status, create_time, update_time)
VALUES
    (1, 'gateway_001', '员工信息查询网关', '用于查询公司员工信息的MCP网关', NULL, 1, '2026-01-02 13:10:19', '2026-01-02 13:10:19');

SELECT setval('mcp_gateway_id_seq', (SELECT MAX(id) FROM mcp_gateway));


-- 转储表 mcp_gateway_auth
-- ------------------------------------------------------------

DROP TABLE IF EXISTS mcp_gateway_auth CASCADE;

CREATE TABLE mcp_gateway_auth (
                                  id BIGSERIAL PRIMARY KEY,
                                  gateway_id VARCHAR(64) NOT NULL,
                                  api_key VARCHAR(128),
                                  rate_limit INTEGER DEFAULT 1000,
                                  expire_time TIMESTAMP,
                                  status SMALLINT NOT NULL DEFAULT 1,
                                  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT uk_user_gateway UNIQUE (gateway_id)
);

COMMENT ON TABLE mcp_gateway_auth IS '用户网关权限表';
COMMENT ON COLUMN mcp_gateway_auth.id IS '主键ID';
COMMENT ON COLUMN mcp_gateway_auth.gateway_id IS '网关ID';
COMMENT ON COLUMN mcp_gateway_auth.api_key IS 'API密钥';
COMMENT ON COLUMN mcp_gateway_auth.rate_limit IS '速率限制（次/小时）';
COMMENT ON COLUMN mcp_gateway_auth.expire_time IS '过期时间';
COMMENT ON COLUMN mcp_gateway_auth.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN mcp_gateway_auth.create_time IS '创建时间';
COMMENT ON COLUMN mcp_gateway_auth.update_time IS '更新时间';

CREATE INDEX idx_gateway_id ON mcp_gateway_auth(gateway_id);
CREATE INDEX idx_api_key ON mcp_gateway_auth(api_key);

INSERT INTO mcp_gateway_auth (id, gateway_id, api_key, rate_limit, expire_time, status, create_time, update_time)
VALUES
    (1, 'gateway_001', 'RS590LKPOD8877DDLMFKS4', 1000, '2029-01-02 16:44:19', 1, '2026-01-02 16:44:19', '2026-01-02 16:44:34');

SELECT setval('mcp_gateway_auth_id_seq', (SELECT MAX(id) FROM mcp_gateway_auth));


-- 转储表 mcp_gateway_tool
-- ------------------------------------------------------------

DROP TABLE IF EXISTS mcp_gateway_tool CASCADE;

CREATE TABLE mcp_gateway_tool (
                                  id BIGSERIAL PRIMARY KEY,
                                  gateway_id VARCHAR(64) NOT NULL,
                                  tool_id BIGINT NOT NULL UNIQUE,
                                  tool_name VARCHAR(128) NOT NULL,
                                  tool_type VARCHAR(32) NOT NULL DEFAULT 'function',
                                  tool_description VARCHAR(512) NOT NULL,
                                  tool_version VARCHAR(16) NOT NULL,
                                  protocol_id BIGINT NOT NULL,
                                  protocol_type VARCHAR(4) NOT NULL DEFAULT 'http',
                                  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT uq_tool_name UNIQUE (gateway_id, tool_name)
);

COMMENT ON TABLE mcp_gateway_tool IS 'MCP工具注册表';
COMMENT ON COLUMN mcp_gateway_tool.id IS '自增ID';
COMMENT ON COLUMN mcp_gateway_tool.gateway_id IS '网关ID';
COMMENT ON COLUMN mcp_gateway_tool.tool_id IS '工具ID';
COMMENT ON COLUMN mcp_gateway_tool.tool_name IS 'MCP工具名称（如：JavaSDKMCPClient_getCompanyEmployee）';
COMMENT ON COLUMN mcp_gateway_tool.tool_type IS '工具类型：function/resource';
COMMENT ON COLUMN mcp_gateway_tool.tool_description IS '工具描述';
COMMENT ON COLUMN mcp_gateway_tool.tool_version IS '工具版本';
COMMENT ON COLUMN mcp_gateway_tool.protocol_id IS '协议ID';
COMMENT ON COLUMN mcp_gateway_tool.protocol_type IS '协议类型；http、dubbo、rabbitmq';
COMMENT ON COLUMN mcp_gateway_tool.create_time IS '创建时间';
COMMENT ON COLUMN mcp_gateway_tool.update_time IS '更新时间';

INSERT INTO mcp_gateway_tool (id, gateway_id, tool_id, tool_name, tool_type, tool_description, tool_version, protocol_id, protocol_type, create_time, update_time)
VALUES
    (1, 'gateway_001', 1, 'JavaSDKMCPClient_getCompanyEmployee', 'function', '获取公司雇员信息', '1.0.0', 1, 'http', '2026-02-01 19:12:44', '2026-02-01 19:46:07');

SELECT setval('mcp_gateway_tool_id_seq', (SELECT MAX(id) FROM mcp_gateway_tool));


-- 转储表 mcp_protocol_http
-- ------------------------------------------------------------

DROP TABLE IF EXISTS mcp_protocol_http CASCADE;

CREATE TABLE mcp_protocol_http (
                                   id BIGSERIAL PRIMARY KEY,
                                   protocol_id BIGINT NOT NULL,
                                   http_url VARCHAR(512) NOT NULL,
                                   http_method VARCHAR(16) NOT NULL DEFAULT 'POST',
                                   http_headers TEXT,
                                   timeout INTEGER DEFAULT 30000,
                                   retry_times SMALLINT DEFAULT 0,
                                   status SMALLINT NOT NULL DEFAULT 1,
                                   create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mcp_protocol_http IS 'MCP工具注册表';
COMMENT ON COLUMN mcp_protocol_http.id IS '主键ID';
COMMENT ON COLUMN mcp_protocol_http.protocol_id IS '协议ID';
COMMENT ON COLUMN mcp_protocol_http.http_url IS 'HTTP接口地址';
COMMENT ON COLUMN mcp_protocol_http.http_method IS 'HTTP请求方法：GET/POST/PUT/DELETE';
COMMENT ON COLUMN mcp_protocol_http.http_headers IS 'HTTP请求头（JSON格式）';
COMMENT ON COLUMN mcp_protocol_http.timeout IS '超时时间（毫秒）';
COMMENT ON COLUMN mcp_protocol_http.retry_times IS '重试次数';
COMMENT ON COLUMN mcp_protocol_http.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN mcp_protocol_http.create_time IS '创建时间';
COMMENT ON COLUMN mcp_protocol_http.update_time IS '更新时间';

INSERT INTO mcp_protocol_http (id, protocol_id, http_url, http_method, http_headers, timeout, retry_times, status, create_time, update_time)
VALUES
    (1, 1, 'http://localhost:8701/api/v1/mcp/get_company_employee', 'post', '{"Content-Type": "application/json"}', 30000, 0, 1, '2026-01-02 13:10:19', '2026-02-01 19:14:57'),
    (3, 2, 'http://localhost:8701/api/v1/mcp/query-by-id', 'get', '{"Content-Type": "application/json"}', 30000, 0, 1, '2026-01-02 13:10:19', '2026-02-01 19:14:58');

SELECT setval('mcp_protocol_http_id_seq', (SELECT MAX(id) FROM mcp_protocol_http));


-- 转储表 mcp_protocol_mapping
-- ------------------------------------------------------------

DROP TABLE IF EXISTS mcp_protocol_mapping CASCADE;

CREATE TABLE mcp_protocol_mapping (
                                      id BIGSERIAL PRIMARY KEY,
                                      protocol_id BIGINT NOT NULL,
                                      mapping_type VARCHAR(32) NOT NULL,
                                      parent_path VARCHAR(256),
                                      field_name VARCHAR(128) NOT NULL,
                                      mcp_path VARCHAR(256) NOT NULL,
                                      mcp_type VARCHAR(32) NOT NULL,
                                      mcp_desc VARCHAR(512),
                                      is_required SMALLINT NOT NULL DEFAULT 0,
                                      sort_order INTEGER DEFAULT 0,
                                      create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mcp_protocol_mapping IS 'MCP映射配置表';
COMMENT ON COLUMN mcp_protocol_mapping.id IS '主键ID';
COMMENT ON COLUMN mcp_protocol_mapping.protocol_id IS '工具ID';
COMMENT ON COLUMN mcp_protocol_mapping.mapping_type IS '映射类型：request-请求参数映射，response-响应数据映射';
COMMENT ON COLUMN mcp_protocol_mapping.parent_path IS '父级路径（如：xxxRequest01，用于构建嵌套结构，根节点为NULL）';
COMMENT ON COLUMN mcp_protocol_mapping.field_name IS '字段名称（如：city、company、name）';
COMMENT ON COLUMN mcp_protocol_mapping.mcp_path IS 'MCP完整路径（如：xxxRequest01.city、xxxRequest01.company.name）';
COMMENT ON COLUMN mcp_protocol_mapping.mcp_type IS 'MCP数据类型：string/number/boolean/object/array';
COMMENT ON COLUMN mcp_protocol_mapping.mcp_desc IS 'MCP字段描述';
COMMENT ON COLUMN mcp_protocol_mapping.is_required IS '是否必填：0-否，1-是（用于生成required数组）';
COMMENT ON COLUMN mcp_protocol_mapping.sort_order IS '排序顺序（同级字段排序）';
COMMENT ON COLUMN mcp_protocol_mapping.create_time IS '创建时间';
COMMENT ON COLUMN mcp_protocol_mapping.update_time IS '更新时间';

CREATE INDEX idx_mapping_type ON mcp_protocol_mapping(mapping_type);
CREATE INDEX idx_parent_path ON mcp_protocol_mapping(parent_path);
CREATE INDEX idx_mcp_path ON mcp_protocol_mapping(mcp_path);
CREATE INDEX idx_sort_order ON mcp_protocol_mapping(sort_order);

INSERT INTO mcp_protocol_mapping (id, protocol_id, mapping_type, parent_path, field_name, mcp_path, mcp_type, mcp_desc, is_required, sort_order, create_time, update_time)
VALUES
    (1, 1, 'request', NULL, 'xxxRequest01', 'xxxRequest01', 'object', NULL, 1, 1, '2026-01-02 13:10:19', '2026-02-01 19:45:59'),
    (2, 1, 'request', 'xxxRequest01', 'city', 'xxxRequest01.city', 'string', '城市名称,如果是中文汉字请先转换为汉语拼音,例如北京:beijing', 1, 1, '2026-01-02 13:10:19', '2026-02-01 19:46:00'),
    (3, 1, 'request', 'xxxRequest01', 'company', 'xxxRequest01.company', 'object', '公司信息,如果是中文汉字请先转换为汉语拼音,例如北京:jd/alibaba', 1, 2, '2026-01-02 13:10:19', '2026-02-01 19:46:01'),
    (4, 1, 'request', 'xxxRequest01.company', 'name', 'xxxRequest01.company.name', 'string', '公司名称', 1, 1, '2026-01-02 13:10:19', '2026-02-01 19:46:01'),
    (5, 1, 'request', 'xxxRequest01.company', 'type', 'xxxRequest01.company.type', 'string', '公司类型', 1, 2, '2026-01-02 13:10:19', '2026-02-01 19:46:02');

SELECT setval('mcp_protocol_mapping_id_seq', (SELECT MAX(id) FROM mcp_protocol_mapping));
