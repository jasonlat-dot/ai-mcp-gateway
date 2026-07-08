<script setup>
/**
 * ToggleSegments — 分段选择控件
 *  - 相比原生 <select>,每个选项可独立配色,所见即所得
 *  - tone 列表: success | info | warning | danger | disabled | violet | default
 *
 * 用法:
 *   <ToggleSegments
 *     v-model="form.auth"
 *     :options="[
 *       { value: 1, label: '强校验', tone: 'success' },
 *       { value: 0, label: '不校验', tone: 'warning' },
 *     ]"
 *   />
 */
defineProps({
  modelValue: { type: [Number, String, Boolean], required: true },
  options:    { type: Array,  required: true },     // [{ value, label, tone }]
  size:       { type: String, default: 'md' },      // 'sm' | 'md'
})
const emit = defineEmits(['update:modelValue'])

function pick(value) {
  emit('update:modelValue', value)
}
</script>

<template>
  <div class="seg" :class="`size-${size}`" role="radiogroup">
    <button
      v-for="opt in options"
      :key="String(opt.value)"
      type="button"
      class="seg-btn"
      :class="[
        `tone-${opt.tone || 'default'}`,
        { active: modelValue === opt.value },
      ]"
      role="radio"
      :aria-checked="modelValue === opt.value"
      @click="pick(opt.value)"
    >
      <span class="seg-dot" />
      <span class="seg-label">{{ opt.label }}</span>
      <span v-if="opt.value !== undefined && opt.value !== null && opt.value !== ''"
            class="seg-tag">{{ opt.value }}</span>
    </button>
  </div>
</template>

<style scoped>
.seg {
  display: inline-flex;
  gap: 8px;
  width: 100%;
}

.size-sm .seg-btn { padding: 5px 10px; font-size: var(--fs-sm); }
.size-md .seg-btn { padding: 8px 12px; font-size: var(--fs-sm); }

.seg-btn {
  flex: 1 1 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 10px;
  border: 1px solid var(--hairline);
  background: var(--bg-deep);
  color: var(--text-muted);
  cursor: pointer;
  font-family: inherit;
  font-weight: 500;
  transition:
    background .15s ease,
    color .15s ease,
    border-color .15s ease,
    transform .05s ease;
  user-select: none;
}

.seg-btn:hover {
  background: var(--bg-card);
  color: var(--text-strong);
}

.seg-btn:active { transform: scale(0.98); }

/* 圆点(色块) — 默认浅色,选中态着色 */
.seg-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.35;
  flex-shrink: 0;
  transition: opacity .15s ease, box-shadow .15s ease;
}

.seg-label {
  white-space: nowrap;
}

.seg-tag {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--fs-2xs);
  padding: 1px 6px;
  border-radius: 6px;
  background: var(--bg-card);
  color: var(--text-muted);
  border: 1px solid var(--hairline);
  line-height: 1.4;
}

/* ----- 选中态:每种 tone 都有对应高亮 ----- */
.seg-btn.active {
  color: var(--text-strong);
  background: var(--bg-card);
  border-color: transparent;
  box-shadow: 0 0 0 1.5px currentColor inset;
  font-weight: var(--fw-semibold);
}
.seg-btn.active .seg-dot {
  opacity: 1;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.05), 0 0 6px currentColor;
}
.seg-btn.active .seg-tag {
  background: transparent;
  color: inherit;
  border-color: currentColor;
}

/* tone 颜色 — 复用全局 CSS 变量 */
.seg-btn.tone-success { color: var(--ok-color); }
.seg-btn.tone-info    { color: var(--primary-600); }
.seg-btn.tone-warning { color: var(--warn-color); }
.seg-btn.tone-danger  { color: var(--err-color); }
.seg-btn.tone-disabled{ color: #f87171; }
.seg-btn.tone-violet  { color: var(--violet-color); }
.seg-btn.tone-default { color: var(--text-muted); }

:root.dark .seg-btn.tone-info { color: var(--primary-300); }
:root.dark .seg-btn.tone-violet { color: #a78bfa; }
</style>