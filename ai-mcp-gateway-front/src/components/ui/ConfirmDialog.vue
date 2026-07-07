<script setup>
/**
 * ConfirmDialog — Glacis 冷色调版本
 * 浅色背景 + 蓝色虚线边框 + 清晰的对错按钮
 */
import { WarningFilled } from "@element-plus/icons-vue";

defineProps({
  title: { type: String, default: '确认操作' },
  desc:  { type: String, default: '' },
  okText:  { type: String, default: '确认' },
  cancelText: { type: String, default: '取消' },
  tone:  { type: String, default: 'danger' },
})
defineEmits(['confirm', 'cancel'])
</script>

<template>
  <el-dialog
    :model-value="true"
    :show-close="false"
    width="420px"
    align-center
    @close="$emit('cancel')"
  >
    <div class="confirm">
      <div class="confirm-glyph" :class="`tone-${tone}`">
        <el-icon><WarningFilled /></el-icon>
      </div>
      <h3 class="confirm-title">{{ title }}</h3>
      <p v-if="desc" class="confirm-desc">{{ desc }}</p>
      <slot />
      <div class="confirm-actions">
        <el-button class="btn-cancel" @click="$emit('cancel')">{{ cancelText }}</el-button>
        <el-button class="btn-confirm" :type="tone === 'primary' ? 'primary' : 'danger'" @click="$emit('confirm')">
          {{ okText }}
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped lang="scss">
.confirm {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 8px;
  padding: 6px 6px 0;
}

.confirm-glyph {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 22px;
  margin-bottom: 6px;
}

.confirm-glyph.tone-danger  { color: var(--rose);   background: var(--rose-soft);   border: 1px solid rgba(190, 24, 93, 0.22); }
.confirm-glyph.tone-warning { color: var(--amber);  background: var(--amber-soft);  border: 1px solid rgba(180, 83, 9, 0.22); }
.confirm-glyph.tone-primary { color: var(--accent); background: var(--accent-soft); border: 1px solid var(--accent-line); }

.confirm-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-strong);
  letter-spacing: -0.01em;
}

.confirm-desc {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.55;
  max-width: 320px;
}

.confirm-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  width: 100%;
  justify-content: center;
}
</style>