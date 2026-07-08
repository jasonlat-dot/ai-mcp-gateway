<script setup>
/**
 * FlatPager — 扁平分页器
 *
 * 风格: dashboard "协议接入时间" 风格
 *   共 N 条 · 第 1 / 4 页 · [‹] [›] · 跳转 [N]
 *   字号 12px, --text-muted, 无背景框
 */
import { computed } from 'vue'

const props = defineProps({
  page:  { type: Number, default: 1 },
  rows:  { type: Number, default: 10 },
  total: { type: Number, default: 0 },
})

const emit = defineEmits(['page-change'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.rows)))
const isFirst = computed(() => props.page <= 1)
const isLast  = computed(() => props.page >= totalPages.value)

function go(page) {
  if (page < 1 || page > totalPages.value || page === props.page) return
  emit('page-change', { page, rows: props.rows })
}

function onJump(e) {
  const v = Number(e.target.value)
  if (Number.isFinite(v)) go(v)
}
</script>

<template>
  <div v-if="total > 0" class="flat-pager">
    <span class="fp-info">共 <b>{{ total }}</b> 条 · 第 <b>{{ page }}</b> / {{ totalPages }} 页</span>
    <button class="fp-btn" :disabled="isFirst" @click="go(page - 1)">‹</button>
    <button class="fp-btn" :disabled="isLast"  @click="go(page + 1)">›</button>
    <span class="fp-jump">
      跳转
      <input
        type="number"
        min="1"
        :max="totalPages"
        :value="page"
        @keyup.enter="onJump"
        @blur="onJump"
      />
    </span>
  </div>
</template>

<style scoped>
.flat-pager {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  font-size: var(--fs-xs);
  color: var(--text-muted);
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  user-select: none;
}

.fp-info b {
  font-weight: var(--fw-semibold);
  color: var(--text-default);
  font-feature-settings: 'tnum';
}

.fp-btn {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-sm);
  color: var(--text-default);
  font-size: var(--fs-base);
  line-height: 1;
  cursor: pointer;
  transition: all var(--dur-fast) var(--ease-glacis);
}
.fp-btn:hover:not(:disabled) {
  border-color: var(--primary-500);
  color: var(--primary-600);
  background: var(--info-soft);
}
:root.dark .fp-btn:hover:not(:disabled) { color: var(--primary-300); }
.fp-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.fp-jump {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.fp-jump input {
  width: 50px;
  height: 28px;
  padding: 0 6px;
  text-align: center;
  background: var(--bg-elevated);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-sm);
  color: var(--text-default);
  font-size: var(--fs-xs);
  font-family: 'JetBrains Mono', monospace;
  font-feature-settings: 'tnum';
  outline: none;
  transition: all var(--dur-fast) var(--ease-glacis);
}
.fp-jump input:focus {
  border-color: var(--primary-500);
  box-shadow: var(--ring-focus-soft);
}
input[type='number']::-webkit-inner-spin-button,
input[type='number']::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>
