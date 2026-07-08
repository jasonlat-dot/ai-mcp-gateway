<script setup>
/**
 * SegmentedControl — Sub2API-inspired
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
  <div class="tabs segmented">
    <button
      v-for="opt in options"
      :key="opt.value"
      class="tab"
      :class="{ 'tab-active': bound === opt.value }"
      @click="pick(opt.value)"
    >
      <span class="seg-label">{{ opt.label }}</span>
      <span v-if="opt.count != null" class="seg-count">{{ opt.count }}</span>
    </button>
  </div>
</template>

<style scoped>
.segmented {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 3px;
  border-radius: var(--radius-lg);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
}

.seg-count {
  min-width: 20px;
  height: 18px;
  line-height: 18px;
  padding: 0 6px;
  border-radius: var(--radius-pill);
  background: var(--bg-deep);
  font-size: var(--fs-3xs);
  font-weight: var(--fw-bold);
  text-align: center;
  color: var(--text-muted);
  font-feature-settings: 'tnum';
}
.tab-active .seg-count {
  background: var(--info-soft);
  color: var(--primary-600);
}
:root.dark .tab-active .seg-count { color: var(--primary-300); }
</style>
