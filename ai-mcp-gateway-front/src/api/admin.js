// 这里不能再写 /admin 了 —— request.js 已经把 baseURL 设成 '/admin'
//
// 后端 controller 已按业务领域拆为 4 个,分别对应以下 4 个前端子模块文件:
//   /admin/gateway/**  -> ./admin/gateway.js     网关基础配置
//   /admin/tool/**     -> ./admin/tool.js         网关工具
//   /admin/protocol/** -> ./admin/protocol.js     网关协议
//   /admin/auth/**     -> ./admin/auth.js         网关认证
//   共享工具           -> ./admin/common.js       copyText / buildSseUrl
//
// 本文件保持聚合导出 —— 现有业务代码 `import { xxx } from '@/api/admin'` 不需要改 import 路径。

export * from './admin/gateway'
export * from './admin/tool'
export * from './admin/protocol'
export * from './admin/auth'
export * from './admin/llm'
export * from './admin/common'
