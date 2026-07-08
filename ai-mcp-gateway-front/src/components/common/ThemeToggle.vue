<script setup>
import { computed, h } from 'vue'
import { Sunny, Moon } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'

const theme = useThemeStore()

// Element Plus 没有 CircleHalf icon — 用内联 SVG 渲染一个半圆
const CircleHalf = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      width: 18,
      height: 18,
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': 2,
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      'aria-hidden': 'true',
    },
    [
      h('circle', { cx: 12, cy: 12, r: 9 }),
      h('path', { d: 'M12 3 V21 A9 9 0 0 0 12 3 Z', fill: 'currentColor' }),
    ],
  )

const icon = computed(() => {
  if (theme.mode === 'system') return CircleHalf
  return theme.isDark ? Moon : Sunny
})

const label = computed(() => {
  if (theme.mode === 'system') return '跟随系统'
  return theme.isDark ? '切换到浅色' : '切换到深色'
})

function onClick() {
  theme.cycle()
}
</script>

<template>
  <button
    type="button"
    class="theme-toggle btn-icon btn-ghost"
    :title="label"
    :aria-label="label"
    @click="onClick"
  >
    <el-icon :size="18"><component :is="icon" /></el-icon>
  </button>
</template>

<style scoped>
.theme-toggle {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  color: var(--text-muted);
  transition: all var(--dur-base) var(--ease-glacis);
}
.theme-toggle:hover {
  color: var(--primary-600);
  background: var(--info-soft);
  transform: rotate(12deg);
}
:root.dark .theme-toggle:hover { color: var(--primary-300); }
</style>
