/* ============================================================
 * useDebug —— 调试模块的 composable
 *
 * 把 App.vue 里散落的调试逻辑(hash 监听 / 开关 / localStorage)
 * 收敛到这一个文件。返回的 reactive 状态可供 DebugPanel /
 * DebugCorner / DebugReopen 共享。
 *
 * 退出调试时:
 *   - 总开关关闭 -> 不显示任何 debug UI,清掉 hash
 *   - 面板关闭 -> 仅隐藏面板,留一个小齿轮供恢复
 *
 * 启动优先级:
 *   URL `?debug=1` / `?debug=0` 优先,
 *   否则读 localStorage(`__loginDemoDebug__`),
 *   都没有则默认开启(开发期便利)。
 *
 * 设计上把"模块级单例状态"和"组件级生命周期"分离开:
 *   - 模块顶层直接初始化 state + bootstrap(),保证 main.js import 时就生效。
 *   - useDebug() 只需在 setup 里调用一次,负责挂载 / 卸载 hashchange 监听。
 * ============================================================ */

import { ref, computed, shallowRef } from 'vue'
import { findStatusByPath, resolveProps } from './statusRegistry.js'

const DEBUG_STORAGE_KEY = '__loginDemoDebug__'

/* —— 模块级单例状态(整个 App 共享,只初始化一次) —— */
const debugEnabled       = ref(true)
const showDebugPanel     = ref(true)
const debugPanelCollapsed = ref(false)
const currentHash        = ref('')
const debugComponent     = shallowRef(null)
const debugProps         = ref({})

function parseHash() {
  return window.location.hash.replace(/^#/, '') || ''
}

/** 决定 debugEnabled 初值。模块加载时立即执行一次。 */
function bootstrap() {
  if (typeof window === 'undefined') return
  const url = new URLSearchParams(window.location.search).get('debug')
  if (url === '1' || url === '0') {
    debugEnabled.value = url === '1'
    localStorage.setItem(DEBUG_STORAGE_KEY, url)
    return
  }
  const stored = localStorage.getItem(DEBUG_STORAGE_KEY)
  if (stored === '1' || stored === '0') {
    debugEnabled.value = stored === '1'
  }
}

/** 根据当前 hash 同步 debugComponent / debugProps */
function syncFromHash() {
  const h = parseHash()
  currentHash.value = h
  const match = findStatusByPath(h)
  if (match) {
    debugComponent.value = match.comp
    debugProps.value = resolveProps(match)
  } else {
    debugComponent.value = null
    debugProps.value = {}
  }
}

/** 跳转状态页;path 为空 = 回登录页 */
function goStatus(path) {
  if (!debugEnabled.value) {
    debugEnabled.value = true
    localStorage.setItem(DEBUG_STORAGE_KEY, '1')
  }
  if (path) {
    window.location.hash = path
  } else {
    history.replaceState(null, '', window.location.pathname + window.location.search)
    currentHash.value = ''
    debugComponent.value = null
  }
}

/** 总开关 */
function toggleDebug() {
  debugEnabled.value = !debugEnabled.value
  localStorage.setItem(DEBUG_STORAGE_KEY, debugEnabled.value ? '1' : '0')
  if (!debugEnabled.value) {
    history.replaceState(null, '', window.location.pathname + window.location.search)
    currentHash.value = ''
    debugComponent.value = null
  }
}

/* 模块加载即初始化(同步执行) */
bootstrap()
syncFromHash()
window.addEventListener('hashchange', syncFromHash)

/* —— composable:组件 setup 里调用一次,获得共享 state 和 actions —— */
export function useDebug() {
  return {
    /* state */
    debugEnabled,
    debugActive: computed(() => debugEnabled.value),
    showDebugPanel,
    debugPanelCollapsed,
    currentHash,
    debugComponent,
    debugProps,
    /* actions */
    goStatus,
    toggleDebug,
  }
}