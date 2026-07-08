<script setup>
import { computed } from 'vue'
import {
  CircleCheckFilled, CircleCloseFilled, WarningFilled, InfoFilled, Close,
} from '@element-plus/icons-vue'
import { useToastStore } from '@/stores/toast'

const store = useToastStore()
const toasts = computed(() => store.items)

function iconFor(type) {
  if (type === 'success') return CircleCheckFilled
  if (type === 'error') return CircleCloseFilled
  if (type === 'warning') return WarningFilled
  return InfoFilled
}

function progressStyle(duration) {
  return { animationDuration: `${duration}ms` }
}
</script>

<template>
  <Teleport to="body">
    <div class="toast-container" role="status" aria-live="polite">
      <transition-group name="toast" tag="div" class="toast-stack">
        <div
          v-for="t in toasts"
          :key="t.id"
          :class="['toast', `toast-${t.type}`]"
        >
          <div class="toast-row">
            <div class="toast-icon">
              <el-icon :size="20"><component :is="iconFor(t.type)" /></el-icon>
            </div>
            <div class="toast-body">
              <div v-if="t.title" class="toast-title">{{ t.title }}</div>
              <div class="toast-message">{{ t.message }}</div>
            </div>
            <button v-if="t.closable" class="toast-close" @click="store.dismiss(t.id)" aria-label="关闭">
              <el-icon :size="14"><Close /></el-icon>
            </button>
          </div>
          <div v-if="t.duration > 0" class="toast-progress" :style="progressStyle(t.duration)"></div>
        </div>
      </transition-group>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-stack { display: flex; flex-direction: column; gap: 10px; }
.toast-leave-active { transition: all 0.25s cubic-bezier(0.4, 0, 1, 1); }
.toast-leave-to { opacity: 0; transform: translateX(20px); }
</style>
