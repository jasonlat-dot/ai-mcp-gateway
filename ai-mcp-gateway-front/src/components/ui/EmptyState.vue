<script setup>
defineProps({
  icon:  { type: String, default: 'Box' },
  title: { type: String, default: '' },
  desc:  { type: String, default: '' },
  tone:  { type: String, default: 'info' },
})
</script>

<template>
  <div class="empty">
    <div class="empty-glyph" :class="`tone-${tone}`">
      <el-icon><component :is="icon" /></el-icon>
    </div>
    <div class="empty-title">{{ title }}</div>
    <div v-if="desc" class="empty-desc">{{ desc }}</div>
    <slot />
  </div>
</template>

<style scoped lang="scss">
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 16px;
  text-align: center;
  gap: 12px;
}

.empty-glyph {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 22px;
  background: var(--bg-deep);
  border: 1px solid var(--hairline);
  color: var(--text-muted);
}

.empty-glyph.tone-info   { color: var(--accent); background: var(--accent-soft); border-color: var(--accent-line); }
.empty-glyph.tone-violet { color: var(--violet); background: var(--violet-soft); border-color: rgba(109, 85, 224, 0.22); }

.empty-title {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--text-strong);
  letter-spacing: -0.01em;
}

.empty-desc {
  font-size: 12.5px;
  color: var(--text-muted);
  max-width: 360px;
  line-height: 1.55;
}
</style>