<script setup>
/**
 * VxeGrid — 项目内统一的数据网格外壳
 *
 * 使用方式:columns + data 通过 props 传入,
 * 但列的 default slot 通过父组件的 <template #col-xxx="{row}"> 注入
 * (在父组件里用 <VxeGrid> 包一层,内含 columns 配置 + 具名插槽)
 *
 *   <VxeGrid
 *     :loading="loading"
 *     :data="list"
 *     :total="total"
 *     :page="query.page"
 *     :rows="query.rows"
 *     empty-icon="Connection"
 *     empty-title="..."
 *     empty-desc="..."
 *     @page-change="onPageChange"
 *   >
 *     <vxe-column field="name" title="Name" min-width="200">
 *       <template #default="{ row }">...</template>
 *     </vxe-column>
 *     ...
 *   </VxeGrid>
 */
import { ref, watch } from 'vue'
import EmptyState from './EmptyState.vue'

const props = defineProps({
  data:        { type: Array,  default: () => [] },
  loading:     { type: Boolean, default: false },
  height:      { type: [String, Number], default: null },
  maxHeight:   { type: [String, Number], default: null },
  size:        { type: String,  default: 'medium' },
  showPager:   { type: Boolean, default: true },
  total:       { type: Number, default: 0 },
  page:        { type: Number, default: 1 },
  rows:        { type: Number, default: 10 },
  emptyIcon:   { type: String, default: 'Box' },
  emptyTitle:  { type: String, default: '暂无数据' },
  emptyDesc:   { type: String, default: '' },
  emptyTone:   { type: String, default: 'info' },
})

const emit = defineEmits(['page-change'])

const pagerConfig = ref({
  total: props.total,
  currentPage: props.page,
  pageSize: props.rows,
  pageSizes: [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: false,
})

watch(
  () => [props.total, props.page, props.rows],
  ([t, p, r]) => {
    pagerConfig.value.total = t
    pagerConfig.value.currentPage = p
    pagerConfig.value.pageSize = r
  },
)

function onPageChange({ currentPage, pageSize }) {
  emit('page-change', { page: currentPage, rows: pageSize })
}
</script>

<template>
  <div class="vxg">
    <vxe-table
      :data="data"
      :loading="loading"
      :height="height"
      :max-height="maxHeight"
      :size="size"
      :column-config="{ resizable: true }"
      :row-config="{ isHover: true, keyField: '_rowKey' }"
      :empty-text="false"
      stripe
      border="none"
      round
      class="vxg-table"
    >
      <template #empty>
        <EmptyState :icon="emptyIcon" :title="emptyTitle" :desc="emptyDesc" :tone="emptyTone" />
      </template>
      <slot />
    </vxe-table>

    <div v-if="showPager" class="vxg-pager">
      <vxe-pager
        v-bind="pagerConfig"
        @page-change="onPageChange"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.vxg {
  width: 100%;
  background: #ffffff;
}

.vxg-table {
  width: 100%;
  font-size: 13.5px;
  color: var(--text-default);
}

/* ===== vxe-table 内置样式覆盖(冷色调浅色版) ===== */
:deep(.vxe-table--render-default .vxe-table--body-wrapper),
:deep(.vxe-table--render-default .vxe-table--header-wrapper) {
  background: transparent;
}

:deep(.vxe-table--header) {
  background: var(--bg-sunken);
}

:deep(.vxe-table--header .vxe-header--column) {
  color: var(--text-strong) !important;
  font-weight: 600 !important;
  font-size: 12.5px !important;
  letter-spacing: 0.02em;
  background: transparent !important;
  height: 44px;
}

:deep(.vxe-table--header .vxe-header--column .vxe-cell) {
  padding: 0 14px;
}

:deep(.vxe-table--body .vxe-body--row) {
  transition: background var(--dur-base) var(--ease-glacis);
}

:deep(.vxe-table--body .vxe-body--row:hover) {
  background: var(--bg-deep) !important;
}

:deep(.vxe-table--body .vxe-body--row.row--current) {
  background: var(--accent-soft) !important;
}

:deep(.vxe-table--body .vxe-body--column) {
  background: transparent !important;
  color: var(--text-default) !important;
  font-size: 13.5px;
  border-bottom: 1px solid var(--hairline-soft) !important;
}

:deep(.vxe-table--body .vxe-body--column .vxe-cell) {
  padding: 12px 14px;
  line-height: 1.5;
}

:deep(.vxe-table--body .vxe-body--row:last-child .vxe-body--column) {
  border-bottom: none !important;
}

:deep(.vxe-table .vxe-table--border-line) {
  border-color: var(--hairline) !important;
}

:deep(.vxe-table .vxe-table--loading) {
  background: rgba(255, 255, 255, 0.72) !important;
}

:deep(.vxe-table .vxe-loading .vxe-loading--spinner) {
  color: var(--accent);
}

.vxg-pager {
  padding: 14px 16px;
  border-top: 1px solid var(--hairline);
  background: var(--bg-sunken);
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

:deep(.vxe-pager) {
  --vxe-pager-bg-color: transparent;
  --vxe-pager-border-color: var(--hairline);
  --vxe-pager-color: var(--text-muted);
  color: var(--text-muted);
  font-weight: 500;
}

:deep(.vxe-pager .vxe-pager--btn),
:deep(.vxe-pager .vxe-pager--num-btn) {
  background: #ffffff !important;
  border: 1px solid var(--hairline) !important;
  color: var(--text-default) !important;
  border-radius: 6px !important;
  min-width: 32px;
  height: 32px;
  transition: all var(--dur-base) var(--ease-glacis);
}

:deep(.vxe-pager .vxe-pager--num-btn.is--active) {
  background: var(--accent) !important;
  border-color: var(--accent) !important;
  color: #ffffff !important;
}

:deep(.vxe-pager .vxe-pager--btn:hover),
:deep(.vxe-pager .vxe-pager--num-btn:hover) {
  border-color: var(--accent) !important;
  color: var(--accent) !important;
}

:deep(.vxe-pager .vxe-pager--num-btn.is--active:hover) {
  color: #ffffff !important;
}

:deep(.vxe-pager .vxe-pager--sizes .vxe-pager--select) {
  border: 1px solid var(--hairline) !important;
  border-radius: 6px !important;
  background: #ffffff;
}
</style>