<script setup>
/**
 * StatusPill — Glacis 冷色调版本
 * tone: success | info | warning | danger | violet | default
 */
defineProps({
  tone: { type: String, default: 'info' },
  label: { type: String, default: '' },
  dot:   { type: Boolean, default: false },     // 默认不再强制显示 dot,改为可选
})
</script>

<template>
  <span class="pill" :class="`tone-${tone}`">
    <span v-if="dot" class="pill-dot" />
    <slot>{{ label }}</slot>
  </span>
</template>

<style scoped lang="scss">
.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0.01em;
  white-space: nowrap;
  border: 1px solid transparent;
  line-height: 1.6;
}

.pill-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.pill.tone-success { color: var(--teal);    background: var(--teal-soft);    border-color: rgba(13, 148, 136, 0.22); }
.pill.tone-info    { color: var(--accent);   background: var(--accent-soft);  border-color: var(--accent-line); }
.pill.tone-warning { color: var(--amber);    background: var(--amber-soft);   border-color: rgba(180, 83, 9, 0.22); }
.pill.tone-danger  { color: var(--rose);     background: var(--rose-soft);    border-color: rgba(190, 24, 93, 0.22); }
.pill.tone-violet  { color: var(--violet);   background: var(--violet-soft);  border-color: rgba(109, 85, 224, 0.22); }
.pill.tone-default { color: var(--text-muted); background: var(--bg-deep);     border-color: var(--hairline); }
</style>