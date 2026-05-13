转换逻辑说明：

1. 从 mcp_protocol_registry 表获取工具基本信息：
   - tool_name → function.name
   - tool_description → function.description
   - tool_type → type (固定为 "function")

2. 从 mcp_protocol_mapping 表构建嵌套的 parameters 结构：
   
   步骤1：查询所有 mapping_type='request' 的记录，按 parent_path 和 sort_order 排序
   
   步骤2：构建层级关系
   - parent_path = NULL 的记录是根节点（如 xxxRequest01, xxxRequest02）
   - parent_path = 'xxxRequest01' 的记录是 xxxRequest01 的子节点
   - parent_path = 'xxxRequest01.company' 的记录是 company 的子节点
   
   步骤3：生成 properties 对象
   - 遍历每个节点，根据 field_name 创建属性
   - 根据 mcp_type 设置 type 字段
   - 根据 mcp_desc 设置 description 字段
   - 如果 mcp_type = 'object'，递归处理其子节点
   
   步骤4：生成 required 数组
   - 在同一层级（相同 parent_path）中，收集所有 is_required=1 的 field_name
   - 将这些 field_name 组成数组，作为该层级的 required 字段

3. 转换示例（基于示例数据）：

   数据库记录：
   | parent_path          | field_name    | mcp_type | is_required | mcp_desc                    |
   |---------------------|---------------|----------|-------------|----------------------------|
   | NULL                | xxxRequest01  | object   | 1           | NULL                       |
   | xxxRequest01        | city          | string   | 1           | 城市名称...                 |
   | xxxRequest01        | company       | object   | 1           | 公司信息...                 |
   | xxxRequest01.company| name          | string   | 1           | 公司名称                    |
   | xxxRequest01.company| type          | string   | 1           | 公司类型                    |

   转换为 MCP JSON：
   {
     "type": "function",
     "function": {
       "name": "JavaSDKMCPClient_getCompanyEmployee",
       "description": "获取公司雇员信息",
       "parameters": {
         "type": "object",
         "properties": {
           "xxxRequest01": {
             "type": "object",
             "properties": {
               "city": {
                 "type": "string",
                 "description": "城市名称,如果是中文汉字请先转换为汉语拼音,例如北京:beijing"
               },
               "company": {
                 "type": "object",
                 "properties": {
                   "name": {
                     "type": "string",
                     "description": "公司名称"
                   },
                   "type": {
                     "type": "string",
                     "description": "公司类型"
                   }
                 },
                 "required": ["name", "type"],
                 "description": "公司信息,如果是中文汉字请先转换为汉语拼音,例如北京:jd/alibaba"
               }
             },
             "required": ["city", "company"]
           }
         },
         "required": ["xxxRequest01", "xxxRequest02"]
       }
     }
   }

4. 转换算法伪代码：

   function buildMCPJson(toolId) {
     // 获取工具基本信息
     tool = SELECT * FROM mcp_tool_registry WHERE id = toolId;
     
     // 获取所有映射配置
     mappings = SELECT * FROM mcp_mapping_config 
                WHERE tool_id = toolId AND mapping_type = 'request'
                ORDER BY parent_path, sort_order;
     
     // 构建层级结构
     properties = {};
     requiredMap = {};
     
     for (mapping in mappings) {
       if (mapping.parent_path == NULL) {
         // 根节点
         properties[mapping.field_name] = {
           type: mapping.mcp_type,
           properties: {},
           required: []
         };
         if (mapping.mcp_desc) {
           properties[mapping.field_name].description = mapping.mcp_desc;
         }
       } else {
         // 子节点：找到父节点并添加
         parentNode = findNodeByPath(properties, mapping.parent_path);
         parentNode.properties[mapping.field_name] = {
           type: mapping.mcp_type
         };
         if (mapping.mcp_desc) {
           parentNode.properties[mapping.field_name].description = mapping.mcp_desc;
         }
         if (mapping.mcp_type == 'object') {
           parentNode.properties[mapping.field_name].properties = {};
           parentNode.properties[mapping.field_name].required = [];
         }
       }
       
       // 收集 required 字段
       if (mapping.is_required == 1) {
         if (!requiredMap[mapping.parent_path]) {
           requiredMap[mapping.parent_path] = [];
         }
         requiredMap[mapping.parent_path].push(mapping.field_name);
       }
     }
     
     // 设置 required 数组
     for (path, fields in requiredMap) {
       node = findNodeByPath(properties, path);
       node.required = fields;
     }
     
     // 组装最终 JSON
     return {
       type: "function",
       function: {
         name: tool.tool_name,
         description: tool.tool_description,
         parameters: {
           type: "object",
           properties: properties,
           required: requiredMap[NULL] || []
         }
       }
     };
   }