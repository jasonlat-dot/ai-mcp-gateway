<script setup>
/**
 * KeyValueCell — 扁平 KV 单元格
 *
 *  - 左:小写 key 标签(灰色)
 *  - 右:主要 value(等宽,加粗)
 *  - 整体可点击复制(可关)
 *  - tone: default | info | warn | success
 */
import { computed } from 'vue'
import { copyText } from '@/api/admin'
import { useToast } from '@/composables/useToast'

const props = defineProps({
  k:        { type: String, default: '' },
  v:        { type: [String, Number, null, undefined], default: '' },
  copyable: { type: Boolean, default: false },
  tone:     { type: String, default: 'default' }, // default | info | warn | success
  copyText_:{ type: String, default: '' },        // 留空 = v
  toastMsg: { type: String, default: '' },        // 留空 = "已复制 {k}={v}"
})

const toast = useToast()

const display = computed(() => {
  if (props.v === null || props.v === undefined || props.v === '') return '—'
  return String(props.v)
})

const valueToCopy = computed(() => (props.copyText_ || (props.v == null ? '' : String(props.v))))

async function onClick(e) {
  if (!props.copyable) return
  e.stopPropagation()
  const val = valueToCopy.value
  if (!val) return
  try {
    await copyText(val)
    toast.success(props.toastMsg || `已复制 ${props.k}=${val}`, { duration: 1800 })
  } catch {
    toast.warning('复制失败,请手动选中', { duration: 2400 })
  }
}
</script>

<template>
  <span
    class="kvc"
    :class="[`tone-${tone}`, { 'is-copyable': copyable && valueToCopy, 'is-empty': !valueToCopy }]"
    :title="copyable ? `点击复制 ${k}` : ''"
    @click="onClick"
  >
    <span class="kvc-k">{{ k }}</span>
    <span class="kvc-eq">=</span>
    <span class="kvc-v">{{ display }}</span>
  </span>
</template>

<style scoped>
.kvc {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--fs-sm);
  line-height: 1.4;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  background: transparent;
  border: 1px solid transparent;
  transition: all var(--dur-fast) var(--ease-glacis);
  min-width: 0;
  box-sizing: border-box;
}

.kvc.is-copyable { cursor: pointer; }
.kvc.is-copyable:hover {
  background: var(--bg-sunken);
  border-color: var(--hairline);
}

.kvc-k {
  color: var(--text-faint);
  font-weight: var(--fw-medium);
  font-size: var(--fs-2xs);
  text-transform: lowercase;
  letter-spacing: 0.02em;
}
.kvc-eq { color: var(--text-faint); font-weight: 500; }
.kvc-v {
  color: var(--text-strong);
  font-weight: var(--fw-semibold);
  font-feature-settings: 'tnum';
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 220px;
  min-width: 0;
  flex: 1;
}

.kvc.is-empty .kvc-v { color: var(--text-faint); font-weight: var(--fw-regular); }

/* tones: 给 value 一个轻色 */
.kvc.tone-info    .kvc-v { color: var(--primary-700); }
:root.dark .kvc.tone-info    .kvc-v { color: var(--primary-300); }
.kvc.tone-warn    .kvc-v { color: var(--warn-color); }
.kvc.tone-success .kvc-v { color: var(--ok-color); }
</style>
