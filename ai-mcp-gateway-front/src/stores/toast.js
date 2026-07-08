import { defineStore } from 'pinia'
import { ref } from 'vue'

let nextId = 1

export const useToastStore = defineStore('toast', () => {
  const items = ref([])
  const DEFAULT_DURATION = 3600

  function push({ type = 'info', title, message, duration = DEFAULT_DURATION, closable = true } = {}) {
    const id = nextId++
    const item = { id, type, title, message, duration, closable, paused: false, startedAt: Date.now(), remaining: duration }
    items.value.push(item)
    if (duration > 0) {
      const t = setTimeout(() => dismiss(id), duration)
      item._timer = t
    }
    return id
  }

  function dismiss(id) {
    const idx = items.value.findIndex((x) => x.id === id)
    if (idx === -1) return
    const it = items.value[idx]
    if (it._timer) clearTimeout(it._timer)
    items.value.splice(idx, 1)
  }

  function clear() {
    for (const it of items.value) if (it._timer) clearTimeout(it._timer)
    items.value = []
  }

  // Convenience
  function success(message, opts = {}) { return push({ ...opts, type: 'success', message }) }
  function error(message, opts = {}) { return push({ ...opts, type: 'error', message }) }
  function warning(message, opts = {}) { return push({ ...opts, type: 'warning', message }) }
  function info(message, opts = {}) { return push({ ...opts, type: 'info', message }) }

  return { items, push, dismiss, clear, success, error, warning, info }
})
