<script setup>
/**
 * HeaderChipsCell — 把 httpHeaders 渲染成多个 chip
 *
 *  - 接受任意格式(JSON 字符串 / 数组 / 对象 / k:v; k=v)
 *  - 敏感头(包含 token/auth/key/secret/passwd/credential/cookie)走 warn 变体
 *  - 可选 click-to-copy:点击整格复制 raw 原文(JSON 字符串优先)
 */
import { computed } from 'vue'
import { parseHeaders, isSensitiveHeader } from '@/utils/format'
import { copyText } from '@/api/admin'
import { useToast } from '@/composables/useToast'

const props = defineProps({
  raw:      { type: [String, Array, Object], default: '' },
  max:      { type: Number, default: 0 },     // 0 = 不折叠
  copyable: { type: Boolean, default: true }, // 点击整格是否复制
  copyText_: { type: String, default: '' },   // 自定义复制内容(留空则用 raw)
})

const toast = useToast()

const items = computed(() => parseHeaders(props.raw))
const visible = computed(() => {
  if (!props.max || items.value.length <= props.max) return items.value
  return items.value.slice(0, props.max)
})
const hiddenCount = computed(() =>
  props.max && items.value.length > props.max ? items.value.length - props.max : 0,
)

const rawForCopy = computed(() => {
  if (props.copyText_) return props.copyText_
  if (props.raw == null) return ''
  if (typeof props.raw === 'string') return props.raw
  try { return JSON.stringify(props.raw) } catch { return String(props.raw) }
})

async function onClick() {
  if (!props.copyable) return
  const v = rawForCopy.value
  if (!v) return
  try {
    await copyText(v)
    toast.success('已复制 Headers', { duration: 1800 })
  } catch {
    toast.warning('复制失败,请手动选中', { duration: 2400 })
  }
}
</script>

<template>
  <span
    class="header-chips-wrap"
    :class="{ 'is-copyable': copyable && rawForCopy }"
    :title="copyable ? '点击复制 Headers' : ''"
    @click="onClick"
  >
    <span v-if="!items.length" class="header-chip header-chip-empty">无 Header</span>
    <template v-else>
      <span
        v-for="(h, i) in visible"
        :key="`${h.k}-${i}`"
        class="header-chip"
        :class="{ 'header-chip-warn': isSensitiveHeader(h.k) }"
        :title="`${h.k}: ${h.v}`"
      >
        <span class="header-k">{{ h.k }}</span>
        <span class="header-eq">=</span>
        <span class="header-v">{{ h.v || '∅' }}</span>
      </span>
      <span v-if="hiddenCount > 0" class="header-chip header-chip-empty">+{{ hiddenCount }}</span>
    </template>
  </span>
</template>

<style scoped>
.header-chips-wrap {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 5px;
  align-items: center;
  min-width: 0;
  max-width: 100%;
  font-family: 'JetBrains Mono', ui-monospace, monospace;
}
.header-chips-wrap.is-copyable { cursor: pointer; }
.header-chips-wrap.is-copyable:hover .header-chip { transform: translateY(-1px); }

.header-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: var(--fs-xs);
  line-height: 1.4;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-default);
  max-width: 100%;
  transition: all var(--dur-base) var(--ease-glacis);
}

.header-chip-warn {
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.16), rgba(245, 158, 11, 0.06));
  border-color: rgba(245, 158, 11, 0.45);
  color: #b45309;
  box-shadow: 0 0 0 1px rgba(245, 158, 11, 0.18) inset;
}
:root.dark .header-chip-warn {
  color: #fbbf24;
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.22), rgba(245, 158, 11, 0.08));
}

.header-chip-empty {
  color: var(--text-faint);
  font-style: italic;
  background: transparent;
  border-style: dashed;
}

.header-k {
  font-weight: 700;
  color: var(--primary-600);
  letter-spacing: 0.01em;
}
:root.dark .header-k { color: var(--primary-300); }
.header-chip-warn .header-k {
  color: inherit;
  text-shadow: 0 0 1px currentColor;
}

.header-eq { color: var(--text-faint); font-weight: 500; }
.header-v {
  color: var(--text-strong);
  font-weight: var(--fw-medium);
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
