# AI MCP Gateway

AI Agent 的 MCP 协议网关服务 — 把任意业务 HTTP 接口转换为 MCP Tool,让大模型可以直接调用。

---

## 一、项目简介

通过可视化配置,把 HTTP 接口一键注册为 MCP Server 中的 Tool,无需写代码:

```
[原始 HTTP 接口]  ──协议映射配置──>  [MCP Tool]
                  ├─ URL / Method / Headers
                  ├─ 入参字段 → JSON Schema
                  └─ 出参字段 → 响应解析
```

**核心特性**

- 零代码协议转换,可视化字段映射
- 每个网关独立签发 API Key,支持速率限制
- 基于 Redis 的分布式会话(支持多实例部署)
- Vue 3 + Element Plus 管理后台

---

## 二、技术栈

| 模块 | 技术 |
|------|------|
| 后端 | Spring Boot 3.4.3 / Java 17 / MyBatis-Plus / Spring AI 1.1.4 / Redisson / Retrofit2 |
| 前端 | Vue 3.4 / Element Plus 2.7 / Pinia / Vite 5 |
| 数据库 | PostgreSQL 14+ |
| 缓存 | Redis 6+(分布式会话 + 限流) |

---

## 三、项目结构

```
ai-mcp-gateway/
├── ai-mcp-gateway-app/            # 应用启动层 (Spring Boot 入口)
├── ai-mcp-gateway-domain/         # 领域层 (DDD 业务逻辑)
├── ai-mcp-gateway-trigger/        # 触发器层 (HTTP 入口)
├── ai-mcp-gateway-infrastructure/ # 基础设施层 (DAO / 端口适配)
├── ai-mcp-gateway-case/           # 用例层 (MCP 消息处理)
├── ai-mcp-gateway-api/            # 对外 API 接口定义
├── ai-mcp-gateway-types/          # 通用枚举 / 异常
└── ai-mcp-gateway-front/          # Vue 3 管理后台
```

---

## 四、快速开始

### 4.1 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| PostgreSQL | 14+ |
| Redis | 6+ |

### 4.2 配置中间件

#### PostgreSQL

默认配置(可在 `ai-mcp-gateway-app/src/main/resources/application-dev.yml` 修改):

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://192.168.3.16:15432/ai_mcp_gateway_v2
    username: root
    password: 123456
```

**初始化数据库:**

```bash
cd docs/dev-ops/pgsql
psql -h 192.168.3.16 -p 15432 -U root -d ai_mcp_gateway_v2 -f ai_mcp_gateway_v2.sql
```

主要表:

| 表名 | 用途 |
|------|------|
| `mcp_gateway` | 网关基础信息 |
| `mcp_gateway_auth` | API Key / 限流 |
| `mcp_gateway_tool` | 工具列表 |
| `mcp_protocol_http` | HTTP 协议配置 (URL/Method/Headers) |
| `mcp_protocol_mapping` | 字段映射 (request/response) |

#### Redis

```yaml
redis:
  sdk:
    config:
      host: 192.168.3.16
      port: 16379
      password: 123456
      database: 2
```

用途:分布式会话存储 + API Key 限流计数。

### 4.3 启动后端

```bash
cd ai-mcp-gateway
mvn clean install -DskipTests
cd ai-mcp-gateway-app
mvn spring-boot:run
```

启动成功后会监听 `http://127.0.0.1:8888`,context path 为 `/api-gateway`。

### 4.4 启动前端

```bash
cd ai-mcp-gateway-front
npm install
npm run dev
```

默认访问 `http://127.0.0.1:5173`,测试账号 `admin / password123`。

### 4.5 访问入口

| 入口 | 地址 |
|------|------|
| 管理后台 | `http://127.0.0.1:5173` |
| 后端 API | `http://127.0.0.1:8888/api-gateway/admin` |
| MCP SSE | `http://127.0.0.1:8888/api-gateway/{gatewayId}/mcp/sse` |

---

## 五、HTTP → MCP 转换原理

```
MCP 客户端 (Claude / Cursor)
        │
        │ 1. GET /{gatewayId}/mcp/sse  建立 SSE 长连接
        │ 2. POST /{gatewayId}/mcp/sse 发送 JSON-RPC 消息
        ▼
McpGatewayController (trigger)
        │
        ▼
IMcpMessageService (case/mcp/sse)
        │
        ├── tools/list  → 加载 mcp_protocol_mapping 生成 JSON Schema
        └── tools/call  → 参数映射 → Retrofit2 调用 HTTP → 响应映射 → SSE 返回
                              │
                              ▼
                       真实业务 HTTP 接口
```

**字段映射示例**(从数据库映射成 JSON Schema):

数据库 `mcp_protocol_mapping` 中按 `parent_path` 构建嵌套结构,`is_required=1` 的字段收集到 `required` 数组。

转换后的 MCP Tool 定义:

```json
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
            "city": { "type": "string", "description": "城市名称" },
            "company": {
              "type": "object",
              "properties": {
                "name": { "type": "string" },
                "type": { "type": "string" }
              },
              "required": ["name", "type"]
            }
          },
          "required": ["city", "company"]
        }
      }
    }
  }
}
```

---

## 六、常见问题

**Q:启动报错端口被占用?**  
修改 `application-dev.yml` 中 `server.port`。

**Q:数据库连接失败?**  
检查 PostgreSQL 是否启动、端口是否开放、`pg_hba.conf` 是否允许密码登录。

**Q:SSE 连接立刻断开?**  
检查 `mcp_gateway_auth` 表中 API Key 的 `expire_time` 和 `status`。

**Q:LLM 客户端找不到 Tool?**  
确认 `mcp_gateway_tool` 工具状态是启用,用 curl 手动调 SSE 端点验证。

---

## 八、许可证

Apache License 2.0 — Author: jasonlat (<2148660566@qq.com>)