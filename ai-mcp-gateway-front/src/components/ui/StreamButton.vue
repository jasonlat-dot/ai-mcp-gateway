<template>
  <button
    :class="['stream-btn', `stream-btn--${variant}`, { 'is-loading': loading }]"
    :type="type"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="stream-btn__spinner" />
    <span v-else class="stream-btn__label">
      <slot />
    </span>
  </button>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'primary' },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  type: { type: String, default: 'button' },
})
defineEmits(['click'])
</script>

<style scoped>
/* ============================================================
   渐变按钮 —— 静态填充,匹配 segmented control active 风格
   primary = 青绿黄渐变 + 白字
   ghost   = 白底 + 浅边 + 深字
   ============================================================ */
.stream-btn {
  position: relative;
  width: 100%;
  height: 46px;
  border-radius: 13px;
  border: 1px solid transparent;

  background: linear-gradient(120deg, #5eead4 0%, #6ee7b7 45%, #fcd34d 100%);
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 8px;
  font-family: inherit;
  cursor: pointer;
  outline: none;

  box-shadow:
    0 6px 18px rgba(94, 234, 212, 0.45),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  transition: transform 0.18s ease, box-shadow 0.3s ease, filter 0.3s ease;
}

.stream-btn:hover {
  filter: brightness(1.04) saturate(1.06);
  box-shadow:
    0 10px 24px rgba(94, 234, 212, 0.55),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transform: translateY(-1px);
}

.stream-btn:active { transform: scale(0.97); }

.stream-btn:disabled,
.stream-btn.is-loading {
  cursor: not-allowed;
  filter: saturate(0.5) brightness(0.96);
  opacity: 0.85;
  box-shadow: 0 4px 10px rgba(94, 234, 212, 0.25);
}

.stream-btn__label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.stream-btn__spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2.5px solid rgba(255, 255, 255, 0.45);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spinner-rot 0.8s linear infinite;
}
@keyframes spinner-rot {
  to { transform: rotate(360deg); }
}

/* —— ghost 变体 —— 白底 + 浅灰边 + 深字 */
.stream-btn--ghost {
  background: rgba(255, 255, 255, 0.94);
  color: rgba(30, 41, 59, 0.85);
  border: 1px solid rgba(186, 230, 253, 0.55);
  box-shadow:
    0 4px 12px -4px rgba(14, 165, 233, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}
.stream-btn--ghost:hover {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(94, 234, 212, 0.6);
  color: #134e4a;
  box-shadow:
    0 8px 18px -4px rgba(94, 234, 212, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 1);
}
</style>