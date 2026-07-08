<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import { startNavigationLoading, finishNavigationLoading, useNavigationLoadingState } from '@/composables/useNavigationLoading'
import router from '@/router'

const { isLoading } = useNavigationLoadingState()

let offBefore = null
let offAfter = null
let offError = null

onMounted(() => {
  offBefore = router.beforeEach((_to, _from, next) => {
    startNavigationLoading()
    next()
  })
  offAfter = router.afterEach(() => {
    finishNavigationLoading()
  })
  offError = router.onError(() => {
    finishNavigationLoading()
  })
})

onBeforeUnmount(() => {
  if (offBefore) offBefore()
  if (offAfter) offAfter()
  if (offError) offError()
})
</script>

<template>
  <transition name="progress-fade">
    <div v-if="isLoading" class="navigation-progress" aria-hidden="true">
      <div class="navigation-progress-bar"></div>
    </div>
  </transition>
</template>

<style scoped>
.progress-fade-enter-active { transition: opacity 0.15s ease-out; }
.progress-fade-leave-active { transition: opacity 0.3s ease-out; }
.progress-fade-enter-from, .progress-fade-leave-to { opacity: 0; }

.navigation-progress {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  z-index: 9999;
  pointer-events: none;
  background: transparent;
  overflow: hidden;
}
.navigation-progress-bar {
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent 0%,
    var(--primary-500) 30%,
    var(--primary-400) 70%,
    transparent 100%
  );
  background-size: 200% 100%;
  animation: progressSlide 1.1s ease-in-out infinite;
  box-shadow: 0 0 12px rgba(20, 184, 166, 0.35);
}
@keyframes progressSlide {
  0%   { transform: translateX(-50%); }
  100% { transform: translateX(50%); }
}
@media (prefers-reduced-motion: reduce) {
  .navigation-progress-bar { animation: none; }
}
</style>
