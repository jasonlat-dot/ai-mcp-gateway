<script setup>
/**
 * DataPager — 简单分页条(供非 VxeGrid 场景使用)
 * 现在 el-pagination 也已被 global.css 重写成冷色调版
 */
import { computed } from 'vue'

const props = defineProps({
  page:    { type: Number, required: true },
  rows:    { type: Number, required: true },
  total:   { type: Number, default: 0 },
  sizes:   { type: Array,  default: () => [10, 20, 50, 100] },
  layout:  { type: String, default: 'total, sizes, prev, pager, next, jumper' },
  background: { type: Boolean, default: true },
  hideOnSingle: { type: Boolean, default: false },
})

const emit = defineEmits(['update:page', 'update:rows', 'change'])

const currentPage = computed({
  get: () => props.page,
  set: (v) => {
    if (v === props.page) return
    emit('update:page', v)
    emit('change', { page: v, rows: props.rows })
  },
})

const pageSize = computed({
  get: () => props.rows,
  set: (v) => {
    if (v === props.rows) return
    emit('update:rows', v)
    emit('update:page', 1)
    emit('change', { page: 1, rows: v })
  },
})
</script>

<template>
  <div class="dp">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="sizes"
      :layout="layout"
      :background="background"
      :hide-on-single-page="hideOnSingle"
    />
  </div>
</template>

<style scoped lang="scss">
.dp {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  width: 100%;
}
</style>