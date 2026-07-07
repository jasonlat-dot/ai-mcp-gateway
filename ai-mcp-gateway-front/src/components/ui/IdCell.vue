<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * IdCell — 统一的 ID 单元格(冷色调版本)
 *   - 等宽字体
 *   - 浅蓝灰底 + 蓝色文字
 *   - 悬停加深 + 显示复制反馈
 *   - 点击复制到剪贴板
 */
const props = defineProps({
  value:    { type: [String, Number], default: '' },
  prefix:   { type: String, default: '#' },
  copyable: { type: Boolean, default: true },
  max:      { type: Number, default: 12 },
})

const display = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '') return '—'
  const raw = String(props.value)
  if (raw.length <= props.max) return `${props.prefix}${raw}`
  const head = raw.slice(0, props.max - 4)
  const tail = raw.slice(-4)
  return `${props.prefix}${head}…${tail}`
})

const full = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '') return ''
  return `${props.prefix}${String(props.value)}`
})

async function onCopy(e) {
  if (!props.copyable || !full.value) return
  e.stopPropagation()
  try {
    await navigator.clipboard.writeText(String(props.value))
    ElMessage.success(`已复制 ${String(props.value)}`)
  } catch {
    ElMessage.warning('复制失败,请手动选中')
  }
}
</script>

<template>
  <span v-if="!full" class="id-cell is-empty">—</span>
  <span
    v-else
    class="id-cell"
    :class="{ 'is-copyable': copyable }"
    :title="full"
    @click="onCopy"
  >
    <span class="id-text">{{ display }}</span>
  </span>
</template>

<style scoped lang="scss">
.id-cell {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 8px;
  border-radius: 6px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--accent);
  font-family: 'JetBrains Mono', 'SFMono-Regular', Menlo, Consolas, monospace;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.01em;
  line-height: 1;
  white-space: nowrap;
  transition: all var(--dur-fast) var(--ease-glacis);
  user-select: text;
}

.id-cell.is-copyable { cursor: pointer; }

.id-cell.is-copyable:hover {
  background: var(--accent-soft);
  border-color: var(--accent-line);
  color: var(--accent-hover);
}

.id-cell.is-empty {
  background: transparent;
  border-color: transparent;
  color: var(--text-faint);
  font-family: inherit;
  font-weight: 400;
}
</style>