# Vanguard · AI MCP Gateway Admin

高级 SaaS 风格的后台管理界面 — 基于 [Vue 3](https://vuejs.org/) + [Element Plus](https://element-plus.org/) + [Pinia](https://pinia.vuejs.org/) + [Vue Router](https://router.vuejs.org/)。

> 设计语言参考 Linear · Vercel · Stripe · Helio 等顶级 SaaS 产品,
> 强调 OLED 深色 + 径向光晕 + 玻璃面板 + 微光内描边带来的"产品感"。
>
> 与 `docs/dev-ops/nginx/html` 中的旧版管理界面仅在**接口层**共享,
> **视觉与结构完全重写**:不再使用 Bootstrap / jQuery,迁移至 Vue 3 现代化架构。

---

## ✨ 页面预览

| 路径            | 名称           | 说明                                                                 |
|-----------------|----------------|----------------------------------------------------------------------|
| `/login`        | 登录           | 双层玻璃 + 渐变描边 + 双侧叙事布局                                  |
| `/dashboard`    | 控制台总览     | Bento 布局:4 卡 stat / 实时最近网关 / 环境状态 / 常用操作 / 协议流水 |
| `/gateways`     | 网关列表       | CRUD + 模糊搜索 + 状态切换 + 复制 SSE URL                            |
| `/gateways/:id` | 网关详情       | 单网关视角:基础信息 / 工具 / 协议 / 认证 三段关联                    |
| `/tools`        | 网关工具       | 工具绑定/解绑,关联协议(实时协议预览)                                |
| `/protocols`    | 协议配置       | 列表 CRUD + OpenAPI 3 步导入(上传 → 选择 → 解析)+ 修改映射           |
| `/protocols/:id`| 协议详情       | 单协议视角:基础字段 + 完整 mappings 表格                             |
| `/auth`         | 认证与限流     | API Key 发放/复制 + 限流速率 + 过期时间                              |

## 🧱 技术栈

- **Vue 3.4** (Composition API + `<script setup>`)
- **Vue Router 4** (Hash 模式便于静态部署)
- **Pinia 2** (轻量状态)
- **Element Plus 2.7** (深度定制为深色玻璃质感,见 `src/styles/global.css`)
- **SCSS** (BEM-ish 风格 + 设计令牌 `src/styles/vars.scss`)
- **axios** (拦截器统一错误处理,见 `src/utils/request.js`)

## 🚀 本地启动

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器(默认监听 :5173)
npm run dev

# 3. 构建生产包
npm run build
```

### 测试账号

> 当前后端没有专门的 auth 接口,登录流程与 `docs/dev-ops/nginx/html` 旧版本一致:

```
账号: admin
密码: password123
```

### 后端代理

Vite 已配置代理(见 `vite.config.js`):

| 前端路径                             | 代理目标(rewrite 后)                                                                            |
|--------------------------------------|--------------------------------------------------------------------------------------------------|
| `/admin/*` (前端 axios 的 baseURL)   | `${VITE_API_BASE}` 默认 `http://127.0.0.1:8888/api-gateway` (带 `/api-gateway/admin/...` 前缀) |
| `/mcp`                               | `${VITE_SSE_BASE}` 默认 `http://127.0.0.1:8888`(用于 SSE 长连接)                                |

> 因为后端 `server.servlet.context-path=/api-gateway`,proxy 用 `rewrite` 把前端的 `/admin/...`
> 翻译成 `/api-gateway/admin/...`,避免 path 重复或漏前缀。

可通过环境变量覆盖:

```bash
VITE_API_BASE=http://localhost:9999/api-gateway \
VITE_SSE_BASE=http://localhost:9999 \
npm run dev
```

## 📁 目录结构

```
src/
├─ api/admin.js                # 接口集合
├─ components/ui/              # 通用 UI 组件 (PageCard / StatCard / StatusPill …)
├─ layouts/AdminLayout.vue     # 双层外壳 · 浮动导航 · 顶栏 · 背景氛围层
├─ router/                     # 路由 + 鉴权守卫
├─ stores/auth.js              # 登录状态 (Pinia)
├─ styles/
│  ├─ theme.css                # 设计令牌 (深色 SaaS 玻璃色系)
│  ├─ global.css               # 重置 + Element Plus 深度定制
│  ├─ index.css                # 入口
│  └─ vars.scss                # SCSS 共享变量 (供组件 <style lang="scss"> 使用)
├─ utils/                      # request / format / 等
└─ views/
   ├─ auth/LoginView.vue
   ├─ DashboardView.vue
   └─ gateway/
      ├─ GatewayListView.vue
      ├─ GatewayDetailView.vue
      ├─ GatewayToolView.vue
      ├─ GatewayProtocolView.vue
      ├─ ProtocolDetailView.vue
      └─ GatewayAuthView.vue
```

> 原有的 `src/components/auth/*` 登录注册组件(动画流)仍然保留,与 `views/auth/LoginView.vue` 并存;
> 该目录后续可以作为更丰富的动画登录方案单独切换。

## 🎨 设计原则

1. **Vibe: Ethereal Glass** — OLED 黑底 + 径向光晕(青/紫)+ 噪点叠加 + 玻璃面板。
2. **Motion: cubic-bezier(0.32, 0.72, 0, 1)** — 所有过渡使用统一的贝塞尔曲线,模拟"上浮 / 沉落"。
3. **Layout: Asymmetrical Bento** — 控制台采用不等分网格,信息密度沿对角线组织。
4. **Component: Double-Bezel** — 主要面板使用 `外层 shell + 内层 core` 嵌套结构,所有圆角互不平行。
5. **Detail: Inset Highlight** — 玻璃面板顶部 1px 内描边光,模拟真实环境光反射。

---

© AI MCP Gateway · Powered by Vanguard UI
