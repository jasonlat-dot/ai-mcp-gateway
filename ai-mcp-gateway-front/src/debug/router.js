/* ============================================================
 * debug router —— 极简 vue-router 注册
 *
 * 用途:让 `useRouter()` / `useRoute()` 在状态页组件里能正常调用而不报错
 * (即使当前没真正用到路由参数,保持路由系统存在可以平滑过渡)。
 *
 * 正式开发时:
 *   - main.js 移除 `import { installDebugRouter } from './debug'`
 *   - 删掉 src/debug/router.js
 *   - 把 App.vue 里的 `<DebugLayer />` 一起移除即可
 * ============================================================ */

import { createRouter, createWebHashHistory } from 'vue-router'
import { statusPages } from './statusRegistry.js'

/**
 * 注册调试用的 vue-router,返回 Promise 让调用方 await isReady()。
 * @param {import('vue').App} app
 */
export function installDebugRouter(app) {
  const router = createRouter({
    history: createWebHashHistory(),
    routes: statusPages.map(p => ({ path: p.path, component: p.comp })),
  })
  app.use(router)
  return router.isReady()
}