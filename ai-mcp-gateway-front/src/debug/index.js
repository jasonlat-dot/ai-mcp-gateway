/* ============================================================
 * src/debug  —— 统一导出
 *
 * 推荐用法(App.vue):
 *   import { DebugLayer, DebugPanel, DebugCorner, DebugReopen, installDebugRouter } from '@/debug'
 *
 * 完整卸载调试模块:
 *   1. main.js 移除 installDebugRouter 那一行
 *   2. App.vue 移除 <DebugLayer /> / <DebugPanel /> / <DebugCorner /> / <DebugReopen />
 *   3. 删除整个 src/debug 目录
 * ============================================================ */

import './styles.css'

export { default as DebugLayer  } from './DebugLayer.vue'
export { default as DebugPanel  } from './DebugPanel.vue'
export { default as DebugCorner } from './DebugCorner.vue'
export { default as DebugReopen } from './DebugReopen.vue'
export { useDebug } from './useDebug.js'
export { installDebugRouter } from './router.js'
export { statusPages, findStatusByPath, resolveProps } from './statusRegistry.js'