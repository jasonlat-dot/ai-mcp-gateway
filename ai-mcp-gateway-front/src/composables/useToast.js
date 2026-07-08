import { useToastStore } from '@/stores/toast'

/**
 * Programmatic toast API.
 *   const toast = useToast()
 *   toast.success('Saved')
 *   toast.error('Network failed', { title: '出错了', duration: 5000 })
 */
export function useToast() {
  const store = useToastStore()
  return {
    success: store.success,
    error: store.error,
    warning: store.warning,
    info: store.info,
    push: store.push,
    dismiss: store.dismiss,
    clear: store.clear,
  }
}
