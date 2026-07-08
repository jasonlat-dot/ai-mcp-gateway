<script setup>
/**
 *  - 抬升面 + 1px hairline + 极轻 shadow
 *  - 顶部 2px 渐变 hairline (hover 时显现)
 *  - 数字更大 (30px),呼吸感增强
 */
defineProps({
  title:  { type: String, default: '' },
  value:  { type: [String, Number], default: 0 },
  delta:  { type: String, default: '' },
  trend:  { type: String, default: 'up' },
  icon:   { type: String, default: 'DataLine' },
  tone:   { type: String, default: 'primary' },
  hint:   { type: String, default: '' },
})
</script>

<template>
  <div class="stat-card card" :class="`tone-${tone}`">
    <div class="stat-top">
      <div class="stat-icon">
        <el-icon><component :is="icon" /></el-icon>
      </div>
      <span v-if="delta" class="stat-delta" :class="`trend-${trend}`">
        <el-icon>
          <component :is="trend === 'down' ? 'CaretBottom' : trend === 'flat' ? 'Minus' : 'CaretTop'" />
        </el-icon>
        {{ delta }}
      </span>
    </div>
    <div class="stat-title">{{ title }}</div>
    <div class="stat-value">{{ value }}</div>
    <div v-if="hint" class="stat-hint">{{ hint }}</div>
  </div>
</template>

<style scoped>
.stat-card { position: relative; }

.stat-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  background: var(--gradient-primary);
  opacity: 0;
  transition: opacity var(--dur-base) var(--ease-glacis);
  border-radius: 2px 2px 0 0;
}
.stat-card:hover::before { opacity: 1; }

.stat-card.tone-primary::before { background: var(--gradient-primary); }
.stat-card.tone-violet::before  { background: linear-gradient(135deg, #a78bfa 0%, #7c3aed 100%); }
.stat-card.tone-mint::before    { background: var(--gradient-primary); }
.stat-card.tone-amber::before   { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); }
.stat-card.tone-rose::before    { background: linear-gradient(135deg, #f87171 0%, #ef4444 100%); }
.stat-card.tone-blue::before    { background: var(--gradient-primary); }

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--input-border-hover);
}

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  display: grid;
  place-items: center;
  font-size: var(--fs-2xl);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-muted);
}

.stat-card.tone-primary .stat-icon,
.stat-card.tone-blue   .stat-icon { color: var(--primary-600); background: var(--info-soft); border-color: var(--info-line); }
.stat-card.tone-violet .stat-icon  { color: var(--violet-color); background: var(--violet-soft); border-color: rgba(139, 92, 246, 0.28); }
.stat-card.tone-mint   .stat-icon  { color: var(--primary-600); background: var(--info-soft); border-color: var(--info-line); }
.stat-card.tone-amber  .stat-icon  { color: var(--warn-color); background: var(--warn-soft); border-color: var(--warn-line); }
.stat-card.tone-rose   .stat-icon  { color: var(--err-color); background: var(--err-soft); border-color: var(--err-line); }
:root.dark .stat-card.tone-primary .stat-icon,
:root.dark .stat-card.tone-blue    .stat-icon,
:root.dark .stat-card.tone-mint    .stat-icon { color: var(--primary-300); }

.stat-delta.trend-up   { color: var(--ok-color); background: var(--ok-soft); }
.stat-delta.trend-down { color: var(--err-color); background: var(--err-soft); }
.stat-delta.trend-flat { color: var(--text-muted); background: var(--bg-deep); }

.stat-delta {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  font-size: var(--fs-2xs);
  font-weight: var(--fw-semibold);
  letter-spacing: var(--ls-wide);
}

.stat-title {
  font-size: var(--fs-sm);
  color: var(--text-muted);
  letter-spacing: var(--ls-wide);
  font-weight: var(--fw-medium);
}

.stat-value {
  margin-top: 4px;
  font-size: var(--fs-4xl);
  font-weight: var(--fw-bold);
  letter-spacing: var(--ls-tight);
  color: var(--text-strong);
  font-feature-settings: 'tnum';
  line-height: var(--lh-tight);
}

.stat-hint {
  margin-top: 6px;
  font-size: var(--fs-xs);
  color: var(--text-faint);
}
</style>
