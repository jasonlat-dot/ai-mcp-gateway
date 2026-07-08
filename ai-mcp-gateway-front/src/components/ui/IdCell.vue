<script setup>
import { computed } from 'vue'
import { copyText } from '@/api/admin'
import { useToast } from '@/composables/useToast'

/**
 * IdCell — 统一的 ID 单元格
 *   - 等宽字体
 *   - 浅底 + 主色文字
 *   - 悬停加深 + 显示复制反馈
 *   - 点击复制到剪贴板(走 copyText,带 textarea 兜底)
 */
const props = defineProps({
  value:    { type: [String, Number], default: '' },
  prefix:   { type: String, default: '#' },
  copyable: { type: Boolean, default: true },
  max:      { type: Number, default: 12 },
  /* truncate: true  => max chars + head…tail ; false => 完整显示,超长换行 */
  truncate: { type: Boolean, default: true },
  /* tone: default(主色) | info | warn | success | danger */
  tone:     { type: String, default: 'default' },
  /* 是否在复制时包含 prefix (#) */
  copyWithPrefix: { type: Boolean, default: false },
  /* 自定义 toast 文案 */
  toastMsg: { type: String, default: '' },
})

const toast = useToast()

const display = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '') return '—'
  const raw = String(props.value)
  if (!props.truncate) return `${props.prefix}${raw}`
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
  const raw = String(props.value)
  const payload = props.copyWithPrefix ? `${props.prefix}${raw}` : raw
  if (!payload) return
  try {
    await copyText(payload)
    toast.success(props.toastMsg || `已复制 ${raw}`, { duration: 2200 })
  } catch {
    toast.warning('复制失败,请手动选中', { duration: 2400 })
  }
}
</script>

<template>
  <span v-if="!full" class="id-cell is-empty">—</span>
  <span
    v-else
    class="id-cell"
    :class="[`tone-${tone}`, { 'is-copyable': copyable, 'is-truncate': truncate }]"
    :title="full"
    @click="onCopy"
  >
    <span class="id-text">{{ display }}</span>
  </span>
</template>

<style scoped>
.id-cell {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  border-radius: var(--radius-sm);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--primary-600);
  font-family: 'JetBrains Mono', 'SFMono-Regular', Menlo, ui-monospace, monospace;
  font-size: var(--fs-xs);
  font-weight: var(--fw-medium);
  letter-spacing: var(--ls-wide);
  line-height: 1;
  white-space: nowrap;
  transition: all var(--dur-fast) var(--ease-glacis);
  user-select: text;
  max-width: 100%;
  min-width: 0;
}
.id-cell .id-text {
  white-space: nowrap;
  overflow-x: auto;
  scrollbar-width: none;
}
.id-cell .id-text::-webkit-scrollbar { display: none; }
.id-cell:not(.is-truncate) .id-text {
  overflow-x: auto;
}

.id-cell.is-copyable { cursor: pointer; }
.id-cell.is-copyable:hover {
  background: var(--info-soft);
  border-color: var(--info-line);
  color: var(--primary-700);
  transform: scale(1.04);
}
:root.dark .id-cell.is-copyable:hover { color: var(--primary-300); }
.id-cell.is-copyable:active { transform: scale(0.97); }

.id-cell.is-empty {
  background: transparent;
  border-color: transparent;
  color: var(--text-faint);
  font-family: inherit;
  font-weight: var(--fw-regular);
}

/* tone variants */
.id-cell.tone-info { color: var(--primary-600); }
.id-cell.tone-warn {
  color: var(--warn-color);
  background: var(--warn-soft);
  border-color: var(--warn-line);
}
.id-cell.tone-success {
  color: var(--ok-color);
  background: var(--ok-soft);
  border-color: var(--ok-line);
}
.id-cell.tone-danger {
  color: var(--err-color);
  background: var(--err-soft);
  border-color: var(--err-line);
}
:root.dark .id-cell.tone-info { color: var(--primary-300); }
</style>
