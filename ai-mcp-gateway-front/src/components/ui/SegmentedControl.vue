<script setup>
/**
 * SegmentedControl — 冷色调版本
 */
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: 'all' },
  options:    { type: Array, required: true },
})

const emit = defineEmits(['update:modelValue', 'change'])

const bound = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

function pick(v) {
  bound.value = v
  emit('change', v)
}
</script>

<template>
  <div class="segmented">
    <button
      v-for="opt in options"
      :key="opt.value"
      class="seg-item"
      :class="{ active: bound === opt.value }"
      @click="pick(opt.value)"
    >
      <span class="seg-label">{{ opt.label }}</span>
      <span v-if="opt.count != null" class="seg-count">{{ opt.count }}</span>
    </button>
  </div>
</template>

<style scoped lang="scss">
.segmented {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 3px;
  border-radius: 9px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
}

.seg-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 7px;
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-muted);
  font-size: 12.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dur-base) var(--ease-glacis);
  white-space: nowrap;
}

.seg-item:hover {
  color: var(--text-strong);
  background: #ffffff;
}

.seg-item.active {
  color: var(--text-strong);
  background: #ffffff;
  border-color: var(--hairline);
  box-shadow: var(--shadow-xs);
}

.seg-count {
  min-width: 20px;
  height: 16px;
  line-height: 16px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--bg-deep);
  font-size: 10.5px;
  font-weight: 700;
  text-align: center;
  color: var(--text-muted);
  font-feature-settings: 'tnum';
}

.seg-item.active .seg-count {
  background: var(--accent-soft);
  color: var(--accent);
}
</style>