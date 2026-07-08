<script setup>
/**
 * StatusPill — Sub2API-inspired Teal system
 * tone: success | info | warning | danger | disabled | violet | default
 */
defineProps({
  tone: { type: String, default: 'info' },
  label: { type: String, default: '' },
  dot:   { type: Boolean, default: false },
})
</script>

<template>
  <span class="badge badge-plain pill" :class="`tone-${tone}`">
    <span v-if="dot" class="pill-dot" />
    <slot>{{ label }}</slot>
  </span>
</template>

<style scoped>
.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: var(--fs-xs);
  font-weight: var(--fw-medium);
  white-space: nowrap;
  border: 1px solid transparent;
  line-height: var(--lh-normal);
}

.pill-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  flex-shrink: 0;
}

.pill.tone-success { color: var(--ok-color); background: var(--ok-soft); border-color: var(--ok-line); }
.pill.tone-info    { color: var(--primary-600); background: var(--info-soft); border-color: var(--info-line); }
.pill.tone-warning { color: var(--warn-color); background: var(--warn-soft); border-color: var(--warn-line); }
.pill.tone-danger  { color: var(--err-color); background: var(--err-soft); border-color: var(--err-line); }
.pill.tone-disabled{ color: #f87171; background: rgba(239, 68, 68, 0.08); border-color: rgba(239, 68, 68, 0.20); }
.pill.tone-violet  { color: var(--violet-color); background: var(--violet-soft); border-color: rgba(139, 92, 246, 0.28); }
.pill.tone-default { color: var(--text-muted); background: var(--bg-deep); border-color: var(--hairline); }

:root.dark .pill.tone-info { color: var(--primary-300); }
:root.dark .pill.tone-violet { color: #a78bfa; }
</style>
