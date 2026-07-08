<script setup>
/**
 * PageCard — 卡片容器
 *  - 抬升面 + 1px hairline + 极轻 shadow
 *  - 标题:粗体 17px,左对齐
 *  - 头部底部 1px 分隔线
 */
defineProps({
  title:    { type: String, default: '' },
  desc:     { type: String, default: '' },
  eyebrow:  { type: String, default: '' },
  padding:  { type: String, default: '22px' },
  hover:    { type: Boolean, default: false },
})
</script>

<template>
  <section class="page-card card" :class="{ 'card-hover': hover }">
    <header v-if="title || $slots.actions" class="card-header" :style="{ padding: `18px ${padding} 16px` }">
      <div class="head-left">
        <span v-if="eyebrow" class="eyebrow">{{ eyebrow }}</span>
        <h2 v-if="title" class="head-title">{{ title }}</h2>
        <p v-if="desc" class="head-desc">{{ desc }}</p>
      </div>
      <div v-if="$slots.actions" class="head-actions">
        <slot name="actions" />
      </div>
    </header>
    <div class="card-body" :style="{ padding }">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.page-card { width: 100%; }

.head-left { min-width: 0; }

.head-title {
  font-size: var(--fs-xl);
  font-weight: var(--fw-semibold);
  letter-spacing: var(--ls-tight);
  color: var(--text-strong);
  margin-top: 10px;
  line-height: var(--lh-snug);
}

.head-desc {
  font-size: var(--fs-sm);
  color: var(--text-muted);
  margin-top: 4px;
  line-height: var(--lh-base);
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.card-body { position: relative; }
</style>
