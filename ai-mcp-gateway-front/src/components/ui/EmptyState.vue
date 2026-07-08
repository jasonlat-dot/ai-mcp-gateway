<script setup>
defineProps({
  icon:  { type: String, default: 'Box' },
  title: { type: String, default: '' },
  desc:  { type: String, default: '' },
  tone:  { type: String, default: 'info' },
})
</script>

<template>
  <div class="empty-state">
    <div class="empty-glyph" :class="`tone-${tone}`">
      <el-icon :size="22"><component :is="icon" /></el-icon>
    </div>
    <div class="empty-state-title">{{ title }}</div>
    <div v-if="desc" class="empty-state-description">{{ desc }}</div>
    <slot />
  </div>
</template>

<style scoped>
.empty-glyph {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-xl);
  display: grid;
  place-items: center;
  background: var(--bg-deep);
  border: 1px solid var(--hairline);
  color: var(--text-muted);
  margin-bottom: 14px;
}
.empty-glyph.tone-info   { color: var(--primary-600); background: var(--info-soft); border-color: var(--info-line); }
.empty-glyph.tone-violet { color: var(--violet-color); background: var(--violet-soft); border-color: rgba(139, 92, 246, 0.28); }
.empty-glyph.tone-success { color: var(--ok-color); background: var(--ok-soft); border-color: var(--ok-line); }
.empty-glyph.tone-warning { color: var(--warn-color); background: var(--warn-soft); border-color: var(--warn-line); }
.empty-glyph.tone-danger  { color: var(--err-color); background: var(--err-soft); border-color: var(--err-line); }
:root.dark .empty-glyph.tone-info { color: var(--primary-300); }
:root.dark .empty-glyph.tone-violet { color: #a78bfa; }
</style>
