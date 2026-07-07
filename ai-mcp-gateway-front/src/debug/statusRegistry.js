/* ============================================================
 * 状态页调试面板 —— 注册表
 *
 * 新增 / 修改状态页:只改这一个文件即可,不用动 App.vue / DebugPanel.vue。
 * 每条记录描述:
 *   - path:    浏览器 hash 路径(以 / 开头)
 *   - label:   面板里显示的中文标签
 *   - icon:    面板里的小 emoji 图标
 *   - component: 实际的 Vue 组件
 *   - props:   进入该页时传给组件的默认 props,便于看到完整效果
 * ============================================================ */

import NotFoundPage       from '../components/status/NotFoundPage.vue'
import ForbiddenPage      from '../components/status/ForbiddenPage.vue'
import ServerErrorPage    from '../components/status/ServerErrorPage.vue'
import NetworkErrorPage   from '../components/status/NetworkErrorPage.vue'
import MaintenancePage    from '../components/status/MaintenancePage.vue'
import EmptyPage          from '../components/status/EmptyPage.vue'
import SessionExpiredPage from '../components/status/SessionExpiredPage.vue'

export const statusPages = [
  { path: '/404',         label: '404 走丢',   icon: '🔍', comp: NotFoundPage,       props: {} },
  { path: '/403',         label: '403 无权限', icon: '🛡️', comp: ForbiddenPage,      props: { role: 'guest' } },
  { path: '/500',         label: '500 服务器', icon: '💥', comp: ServerErrorPage,    props: { traceId: 'ERR-20260704-8A3F1', retryAfter: 0 } },
  { path: '/network',     label: '网络异常',   icon: '📡', comp: NetworkErrorPage,   props: { lastError: 'net::ERR_CONNECTION_REFUSED' } },
  { path: '/maintenance', label: '维护中',     icon: '🛠️', comp: MaintenancePage,    props: { endTime: () => Date.now() + 3600_000, announce: '' } },
  { path: '/empty',       label: '空数据',     icon: '📭', comp: EmptyPage,          props: {} },
  { path: '/session',     label: '登录过期',   icon: '🔐', comp: SessionExpiredPage, props: { remainSeconds: 90, redirect: '/' } },
]

/** 根据 hash 路径查找匹配的注册项;未匹配返回 null */
export function findStatusByPath(path) {
  return statusPages.find(p => p.path === path) || null
}

/**
 * 解析注册项里可能为函数的 props(比如 endTime 想要每次进入都重新计算),
 * 返回普通对象,可直接 spread 给组件。
 */
export function resolveProps(entry) {
  const out = {}
  for (const [k, v] of Object.entries(entry.props || {})) {
    out[k] = typeof v === 'function' ? v() : v
  }
  return out
}