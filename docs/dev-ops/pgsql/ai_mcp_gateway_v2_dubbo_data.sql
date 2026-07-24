-- ************************************************************
-- PostgreSQL 一次性脚本:为 EmployeeService RPC 注册 MCP 工具
-- 适用版本: ai_mcp_gateway_v2
-- 前置: 已运行 ai_mcp_gateway_v2_dubbo.sql 建好 mcp_protocol_dubbo 表
-- 幂等: 脚本可重复运行(已存在则跳过)
-- ************************************************************

BEGIN;

-- 0) 幂等保护:已注册过同名工具就退出
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM mcp_gateway_tool
        WHERE gateway_id = 'gateway_001'
          AND tool_name = 'JavaSDKMCPClient_getCompanyEmployeeByDubbo'
    ) THEN
        RAISE NOTICE 'tool already registered, skip.';
        RETURN;
    END IF;
END $$;

-- 1) 在 mcp_gateway_tool 注册一条 protocol_type='DUBBO' 的工具
--    tool_id=2 避免与已有 HTTP 工具的 tool_id=1 冲突
--    protocol_id=2 避免与已有 HTTP 工具的 protocol_id=1 冲突
INSERT INTO mcp_gateway_tool (gateway_id, tool_id, tool_name, tool_type, tool_description,
                              tool_version, protocol_id, protocol_type, create_time, update_time)
VALUES ('gateway_001', 2, 'JavaSDKMCPClient_getCompanyEmployeeByDubbo', 'function',
        '通过 Dubbo RPC 获取公司雇员信息', '1.0.0', 2, 'DUBBO',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2) 在 mcp_protocol_dubbo 插入协议配置,protocol_id = 2
INSERT INTO mcp_protocol_dubbo (protocol_id, interface_name, group_name, version, method_name,
                                parameter_types, timeout, retry_times, status,
                                create_time, update_time)
VALUES (2, 'com.jasonlat.ai.dubbo.api.EmployeeService', 'default', '1.0.0',
        'getCompanyEmployee',
        '["com.jasonlat.ai.dubbo.api.dto.EmployeeRequest"]',
        5000, 0, 1,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3) 字段映射:request — 描述 LLM 传进来的 schema
--    对应 EmployeeRequest:{ city: string, company: { name: string, type: string } }
INSERT INTO mcp_protocol_mapping (protocol_id, mapping_type, parent_path, field_name, mcp_path,
                                  mcp_type, mcp_desc, is_required, sort_order,
                                  create_time, update_time) VALUES
    (2, 'request', NULL, 'xxxRequest01', 'xxxRequest01', 'object', NULL, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'request', 'xxxRequest01', 'city', 'xxxRequest01.city', 'string',
     '城市名称,如果是中文汉字请先转换为汉语拼音,例如北京:beijing', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'request', 'xxxRequest01', 'company', 'xxxRequest01.company', 'object',
     '公司信息', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'request', 'xxxRequest01.company', 'name', 'xxxRequest01.company.name', 'string',
     '公司名称', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'request', 'xxxRequest01.company', 'type', 'xxxRequest01.company.type', 'string',
     '公司类型', 0, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4) 字段映射:response — 描述 Provider 返回值的 schema
--    对应 EmployeeResponse:{ code, message, city, companyName, employeeCount, employees[] }
INSERT INTO mcp_protocol_mapping (protocol_id, mapping_type, parent_path, field_name, mcp_path,
                                  mcp_type, mcp_desc, is_required, sort_order,
                                  create_time, update_time) VALUES
    (2, 'response', NULL, 'code', 'code', 'number', '业务状态码,0 表示成功', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'response', NULL, 'message', 'message', 'string', '状态描述', 0, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'response', NULL, 'city', 'city', 'string', '回传查询城市', 0, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'response', NULL, 'companyName', 'companyName', 'string', '回传查询公司', 0, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'response', NULL, 'employeeCount', 'employeeCount', 'number', '员工总数', 0, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'response', NULL, 'employees', 'employees', 'array', '员工列表', 0, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

COMMIT;

-- 验证(查询结果预期 3 行:1 tool + 1 dubbo config + 11 mapping)
SELECT 'tool'        AS kind, id, tool_name       AS name, protocol_id::text AS protocol_id FROM mcp_gateway_tool
WHERE tool_name = 'JavaSDKMCPClient_getCompanyEmployeeByDubbo'
UNION ALL
SELECT 'dubbo_cfg'   AS kind, id, interface_name  AS name, protocol_id::text            FROM mcp_protocol_dubbo WHERE protocol_id = 2
UNION ALL
SELECT 'mapping'     AS kind, id, mcp_path        AS name, mapping_type                 FROM mcp_protocol_mapping WHERE protocol_id = 2
ORDER BY kind, id;
