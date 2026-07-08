import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

const STORAGE_KEY = 'mcp.theme'

function readInitial() {
  if (typeof window === 'undefined') return 'light'
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored === 'light' || stored === 'dark' || stored === 'system') return stored
  } catch (_) {
    // ignore
  }
  return 'light'
}

function systemPrefersDark() {
  if (typeof window === 'undefined') return false
  return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
}

function applyDarkClass(isDark) {
  if (typeof document === 'undefined') return
  const root = document.documentElement
  if (isDark) {
    root.classList.add('dark')
  } else {
    root.classList.remove('dark')
  }
  root.style.colorScheme = isDark ? 'dark' : 'light'
}

export const useThemeStore = defineStore('theme', () => {
  const mode = ref(readInitial())   // 'light' | 'dark' | 'system'
  const isDark = ref(false)

  function evaluate() {
    if (mode.value === 'system') {
      isDark.value = systemPrefersDark()
    } else {
      isDark.value = mode.value === 'dark'
    }
    applyDarkClass(isDark.value)
  }

  function setMode(next) {
    mode.value = next
    try { localStorage.setItem(STORAGE_KEY, next) } catch (_) { /* ignore */ }
    evaluate()
  }

  function toggle() {
    // 3-state cycle: light -> dark -> system -> light
    if (mode.value === 'light') setMode('dark')
    else if (mode.value === 'dark') setMode('system')
    else setMode('light')
  }

  function cycle() {
    // 2-state quick toggle (used by topbar button)
    if (isDark.value) setMode('light')
    else setMode('dark')
  }

  const label = computed(() => {
    if (mode.value === 'system') return isDark.value ? '跟随系统 (深色)' : '跟随系统 (浅色)'
    return isDark.value ? '深色' : '浅色'
  })

  // Apply on store init
  evaluate()

  // React to system preference changes while in 'system' mode
  if (typeof window !== 'undefined' && window.matchMedia) {
    const mql = window.matchMedia('(prefers-color-scheme: dark)')
    const handler = () => { if (mode.value === 'system') evaluate() }
    if (mql.addEventListener) mql.addEventListener('change', handler)
    else if (mql.addListener) mql.addListener(handler)
  }

  // Persist when mode changes
  watch(mode, (v) => {
    try { localStorage.setItem(STORAGE_KEY, v) } catch (_) { /* ignore */ }
  })

  return { mode, isDark, label, setMode, toggle, cycle, apply: evaluate }
})
