<script setup>
import { ref, onErrorCaptured, onMounted, getCurrentInstance } from 'vue'
import { RouterView } from 'vue-router'
import NavigationProgress from '@/components/common/NavigationProgress.vue'
import ToastHost from '@/components/common/ToastHost.vue'
import { useThemeStore } from '@/stores/theme'
import { useToast } from '@/composables/useToast'

const theme = useThemeStore()
onMounted(() => theme.apply())

const toast = useToast()
// 只在 dev 模式兜底:真实用户不应该看到红条,
const isDev = import.meta.env.DEV

// Diagnostic banner — 如果路由组件 render/setup 阶段抛错,在此处显示。
// 业务 API 错误(try/catch 漏掉的)也会冒到这里,我们以 toast 形式提示用户。
const captured = ref(null)
onErrorCaptured((err) => {
  // eslint-disable-next-line no-console
  console.error('[App errorCaptured]', err)
  // 业务错误用 toast 提示;render 错误才显示红条。
  const isRenderError = err && (err instanceof Error || typeof err === 'string')
  if (!isDev) {
    toast.error('页面出现异常,请稍后重试')
  } else {
    captured.value = {
      message: err?.message || String(err),
      stack: err?.stack || '',
    }
  }
  return false
})

function dismiss() {
  captured.value = null
}
</script>

<template>
  <RouterView />
  <NavigationProgress />
  <ToastHost />

  <Transition name="fade">
    <div v-if="captured" class="app-error" role="alert">
      <div class="app-error-inner">
        <div class="app-error-head">
          <div class="app-error-title">页面渲染出现异常 (dev only)</div>
          <button class="app-error-close" type="button" aria-label="关闭" @click="dismiss">×</button>
        </div>
        <pre class="msg">{{ captured.message }}</pre>
        <details v-if="captured.stack">
          <summary>堆栈</summary>
          <pre class="stack">{{ captured.stack }}</pre>
        </details>
      </div>
    </div>
  </Transition>
</template>

<style>
.route-fade-enter-active,
.route-fade-leave-active { transition: opacity .2s ease; }
.route-fade-enter-from,
.route-fade-leave-to { opacity: 0; }

/* dev-only error banner — sits in the lower-right, never blocks the rest of the UI */
.app-error {
  position: fixed;
  right: 16px;
  bottom: 16px;
  z-index: 10000;
  max-width: 520px;
  pointer-events: none;
}
.app-error-inner {
  pointer-events: auto;
  background: rgba(15, 23, 42, 0.96);
  color: #f8fafc;
  border: 1px solid rgba(239, 68, 68, 0.5);
  border-radius: 10px;
  padding: 14px 16px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 12.5px;
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.32);
}
.app-error-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}
.app-error-title {
  color: #f87171;
  font-weight: 700;
  font-size: 12.5px;
  font-family: inherit;
}
.app-error-close {
  appearance: none;
  background: transparent;
  border: 0;
  color: #94a3b8;
  font-size: 20px;
  line-height: 1;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: background .15s ease, color .15s ease;
}
.app-error-close:hover {
  background: rgba(248, 250, 252, 0.08);
  color: #f8fafc;
}
.app-error-inner pre {
  margin: 4px 0 0;
  white-space: pre-wrap;
  word-break: break-word;
  color: #f8fafc;
}
.app-error-inner pre.msg {
  max-height: 80px;
  overflow: auto;
}
.app-error-inner pre.stack {
  margin-top: 6px;
  max-height: 180px;
  overflow: auto;
  color: #94a3b8;
  font-size: 11.5px;
}
.app-error-inner details {
  margin-top: 6px;
  font-family: inherit;
}
.app-error-inner summary {
  cursor: pointer;
  color: #5eead4;
  font-family: inherit;
  font-size: 11.5px;
}

.fade-enter-active, .fade-leave-active { transition: opacity .15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
