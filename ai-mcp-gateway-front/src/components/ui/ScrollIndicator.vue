<script setup>
/**
 * ScrollIndicator — 横向滚动指示器
 *
 * 当父级出现横向溢出时,显示一条进度条,滑块位置/宽度
 * 跟随 scrollLeft/scrollWidth/clientWidth 同步。
 *
 * 用法:
 *   <ScrollIndicator
 *     :scroll-left="scrollLeft"
 *     :scroll-width="scrollWidth"
 *     :client-width="clientWidth"
 *   />
 */
import { computed } from 'vue'

const props = defineProps({
  scrollLeft:  { type: Number, default: 0 },
  scrollWidth: { type: Number, default: 0 },
  clientWidth: { type: Number, default: 0 },
})

const ratio = computed(() => {
  if (!props.scrollWidth || props.scrollWidth <= props.clientWidth) return 0
  return Math.min(1, Math.max(0, props.scrollLeft / (props.scrollWidth - props.clientWidth)))
})

const thumbWidth = computed(() => {
  if (!props.scrollWidth) return '100%'
  const w = (props.clientWidth / props.scrollWidth) * 100
  return `${Math.max(10, Math.min(100, w))}%`
})

const thumbOffset = computed(() => {
  const max = 100 - Math.max(10, Math.min(100, (props.clientWidth / props.scrollWidth) * 100))
  return `calc(${ratio.value * max}%)`
})
</script>

<template>
  <div class="si">
    <div class="si-track">
      <div
        class="si-thumb"
        :style="{ width: thumbWidth, transform: `translateX(${ratio * 100}%)` }"
      />
    </div>
  </div>
</template>

<style scoped>
.si {
  flex: 1 1 240px;
  min-width: 200px;
  max-width: 360px;
  display: flex;
  align-items: center;
  user-select: none;
}

.si-track {
  position: relative;
  width: 100%;
  height: 6px;
  border-radius: 999px;
  background: var(--bg-deep);
  border: 1px solid var(--hairline);
  overflow: hidden;
}

.si-thumb {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, var(--primary-400), var(--primary-500));
  border-radius: 999px;
  box-shadow: 0 1px 4px rgba(20, 184, 166, 0.35);
  transition: transform 80ms linear, width 120ms var(--ease-glacis);
}

:root.dark .si-track {
  background: rgba(255, 255, 255, 0.05);
}
:root.dark .si-thumb {
  box-shadow: 0 0 8px rgba(94, 234, 212, 0.45);
}
</style>