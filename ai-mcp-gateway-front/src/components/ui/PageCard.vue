<script setup>
/**
 * PageCard — 卡片容器(冷色调版)
 *  - 白底 + 1px hairline + 极淡 shadow
 *  - 标题:粗体 16-17px,左对齐
 *  - 头部有底部 1px 分隔线,标题区与内容区清晰分层
 */
defineProps({
  title:    { type: String, default: '' },
  desc:     { type: String, default: '' },
  eyebrow:  { type: String, default: '' },
  padding:  { type: String, default: '22px' },
})
</script>

<template>
  <section class="page-card">
    <header v-if="title || $slots.actions" class="card-head" :style="{ padding: `20px ${padding} 18px` }">
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

<style scoped lang="scss">
.page-card {
  width: 100%;
  background: #ffffff;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xs);
  overflow: hidden;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  border-bottom: 1px solid var(--hairline-soft);
}

.head-left { min-width: 0; }

.head-title {
  font-size: 16.5px;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: var(--text-strong);
  margin-top: 10px;
}

.head-desc {
  font-size: 12.5px;
  color: var(--text-muted);
  margin-top: 4px;
  line-height: 1.55;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.card-body { position: relative; }
</style>