<script setup>
/**
 * HeaderChips — HTTP Headers 渲染
 *  - 接受后端的 httpHeaders 原始字段(字符串 JSON / 数组 / map / k:v;k:v)
 *  - 解析后渲染为 `k = v` 高亮 chip
 *  - 包含 token / auth / key / secret / passwd / credential / cookie 的 key 自动琥珀高亮
 *  - 空值显示占位(默认 "无 Header",可自定义)
 *  - 用法:
 *      <HeaderChips :raw="p.httpHeaders" />
 *      <HeaderChips :headers="[{ k, v }]" placeholder="-" />
 */
import { computed } from 'vue'
import { parseHeaders, isSensitiveHeader } from '@/utils/format'

const props = defineProps({
  raw:        { type: [String, Array, Object], default: null },
  headers:    { type: Array, default: null },    // 直接传 [{k,v}] 数组优先
  placeholder:{ type: String, default: '无 Header' },
  max:        { type: Number, default: 20 },     // 单个 v 的最大字符截断
  size:       { type: String, default: 'md' },   // 'sm' | 'md'
})

const list = computed(() => {
  if (props.headers && props.headers.length) return props.headers
  return parseHeaders(props.raw)
})

function truncate(v) {
  if (!v) return ''
  if (props.max > 0 && v.length > props.max) return v.slice(0, props.max) + '…'
  return v
}
</script>

<template>
  <div v-if="!list.length" class="hc-empty" :class="`size-${size}`">{{ placeholder }}</div>
  <div v-else class="hc-list" :class="`size-${size}`">
    <span
      v-for="(h, i) in list"
      :key="i"
      class="hc-chip"
      :class="{ 'hc-warn': isSensitiveHeader(h.k) }"
    >
      <span class="hc-k">{{ h.k }}</span>
      <span class="hc-eq">=</span>
      <span class="hc-v" :title="String(h.v)">{{ truncate(h.v) || '∅' }}</span>
    </span>
  </div>
</template>

<style scoped>
.hc-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-width: 0;
}
.hc-empty {
  color: var(--text-faint);
  font-style: italic;
  font-size: var(--fs-xs);
  padding: 3px 10px;
  border-radius: var(--radius-pill);
  background: var(--bg-sunken);
  border: 1px dashed var(--hairline);
  display: inline-flex;
  align-items: center;
  line-height: 1.4;
}

.hc-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: var(--radius-pill);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-default);
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  line-height: 1.4;
  max-width: 100%;
  transition: all var(--dur-fast) var(--ease-glacis);
}
.hc-chip:hover {
  background: var(--bg-elevated);
  border-color: var(--input-border-hover);
  transform: translateY(-1px);
}

.size-sm .hc-chip { padding: 2px 8px; font-size: var(--fs-2xs); }
.size-md .hc-chip { font-size: var(--fs-xs); }

.hc-k {
  font-weight: var(--fw-bold);
  color: var(--primary-600);
  letter-spacing: var(--ls-wide);
}
.hc-eq {
  color: var(--text-faint);
  font-weight: var(--fw-regular);
}
.hc-v {
  color: var(--text-strong);
  font-weight: var(--fw-semibold);
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 敏感 header — 琥珀高亮 + 模糊值 */
.hc-warn {
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.16), rgba(245, 158, 11, 0.06));
  border-color: rgba(245, 158, 11, 0.45);
  color: #b45309;
  box-shadow: 0 0 0 1px rgba(245, 158, 11, 0.18) inset;
}
.hc-warn .hc-k { color: inherit; text-shadow: 0 0 1px currentColor; }
.hc-warn .hc-v {
  filter: blur(0.3px);
  font-feature-settings: 'tnum';
}

:root.dark .hc-warn {
  color: #fbbf24;
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.22), rgba(245, 158, 11, 0.08));
}
:root.dark .hc-k { color: var(--primary-300); }
</style>
