<script setup>
/**
 * StatCard — 冷色调版本
 *  - 白底 + 极淡 shadow + 顶部 2px 彩色 accent hairline
 *  - 数字更大 (32px),呼吸感增强
 */
defineProps({
  title:  { type: String, default: '' },
  value:  { type: [String, Number], default: 0 },
  delta:  { type: String, default: '' },
  trend:  { type: String, default: 'up' },
  icon:   { type: String, default: 'DataLine' },
  tone:   { type: String, default: 'blue' },
  hint:   { type: String, default: '' },
})
</script>

<template>
  <div class="stat-card" :class="`tone-${tone}`">
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

<style scoped lang="scss">
.stat-card {
  position: relative;
  background: #ffffff;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
  padding: 20px 22px;
  box-shadow: var(--shadow-xs);
  transition: all var(--dur-base) var(--ease-glacis);
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 18px;
  right: 18px;
  height: 2px;
  background: var(--accent);
  opacity: 0.85;
  border-radius: 0 0 2px 2px;
}

.stat-card.tone-blue::before   { background: var(--accent); }
.stat-card.tone-violet::before { background: var(--violet); }
.stat-card.tone-mint::before   { background: var(--teal); }
.stat-card.tone-amber::before  { background: var(--amber); }
.stat-card.tone-rose::before   { background: var(--rose); }

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--hairline-strong);
}

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  font-size: 17px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-muted);
}

.stat-card.tone-blue   .stat-icon { color: var(--accent); background: var(--accent-soft); border-color: var(--accent-line); }
.stat-card.tone-violet .stat-icon { color: var(--violet); background: var(--violet-soft); border-color: rgba(109, 85, 224, 0.22); }
.stat-card.tone-mint   .stat-icon { color: var(--teal);   background: var(--teal-soft);   border-color: rgba(13, 148, 136, 0.22); }
.stat-card.tone-amber  .stat-icon { color: var(--amber);  background: var(--amber-soft);  border-color: rgba(180, 83, 9, 0.22); }
.stat-card.tone-rose   .stat-icon { color: var(--rose);   background: var(--rose-soft);   border-color: rgba(190, 24, 93, 0.22); }

.stat-delta {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

.stat-delta.trend-up   { color: var(--teal);  background: var(--teal-soft); }
.stat-delta.trend-down { color: var(--rose);  background: var(--rose-soft); }
.stat-delta.trend-flat { color: var(--text-muted); background: var(--bg-deep); }

.stat-title {
  font-size: 12.5px;
  color: var(--text-muted);
  letter-spacing: 0.01em;
  font-weight: 500;
}

.stat-value {
  margin-top: 4px;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.04em;
  color: var(--text-strong);
  font-feature-settings: 'tnum';
  line-height: 1.1;
}

.stat-hint {
  margin-top: 6px;
  font-size: 11.5px;
  color: var(--text-faint);
}
</style>