<script setup>
/**
 * ConfirmDialog — Sub2API-inspired
 * 抬升面 + 彩色 glyph + 清晰的对错按钮
 */
import { WarningFilled } from "@element-plus/icons-vue";

defineProps({
  title: { type: String, default: '确认操作' },
  desc:  { type: String, default: '' },
  okText:  { type: String, default: '确认' },
  cancelText: { type: String, default: '取消' },
  tone:  { type: String, default: 'danger' },
  /* true 时禁用确认按钮(比如仍有引用不允许删除)。点击不会 emit confirm */
  okDisabled: { type: Boolean, default: false },
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
        <el-button
          class="btn-confirm"
          :type="tone === 'primary' ? 'primary' : 'danger'"
          :disabled="okDisabled"
          @click="$emit('confirm')"
        >
          {{ okText }}
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
.confirm {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 8px;
  padding: 6px 6px 0;
}

.confirm-glyph {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-xl);
  display: grid;
  place-items: center;
  font-size: var(--fs-3xl);
  margin-bottom: 6px;
  border: 1px solid transparent;
}
.confirm-glyph.tone-danger  { color: var(--err-color); background: var(--err-soft); border-color: var(--err-line); }
.confirm-glyph.tone-warning { color: var(--warn-color); background: var(--warn-soft); border-color: var(--warn-line); }
.confirm-glyph.tone-primary { color: var(--primary-600); background: var(--info-soft); border-color: var(--info-line); }
:root.dark .confirm-glyph.tone-primary { color: var(--primary-300); }

.confirm-title {
  font-size: var(--fs-xl);
  font-weight: var(--fw-semibold);
  color: var(--text-strong);
  letter-spacing: var(--ls-snug);
}

.confirm-desc {
  font-size: var(--fs-sm);
  color: var(--text-muted);
  line-height: var(--lh-base);
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
