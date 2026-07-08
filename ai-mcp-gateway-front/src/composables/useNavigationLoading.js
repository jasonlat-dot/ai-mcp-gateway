import { ref } from 'vue'

// Singleton ref — module-level so every consumer shares state.
const isLoading = ref(false)
const MIN_VISIBLE_MS = 220
let showTimer = null
let hideTimer = null
let visibleSince = 0

export function startNavigationLoading() {
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
  if (!isLoading.value) {
    isLoading.value = true
    visibleSince = Date.now()
  } else {
    // already visible — just push the timestamp forward
    visibleSince = Date.now()
  }
}

export function finishNavigationLoading() {
  if (!isLoading.value) return
  const elapsed = Date.now() - visibleSince
  const remain = Math.max(0, MIN_VISIBLE_MS - elapsed)
  if (remain === 0) {
    isLoading.value = false
  } else if (!hideTimer) {
    hideTimer = setTimeout(() => {
      isLoading.value = false
      hideTimer = null
    }, remain)
  }
}

export function useNavigationLoadingState() {
  return { isLoading }
}
