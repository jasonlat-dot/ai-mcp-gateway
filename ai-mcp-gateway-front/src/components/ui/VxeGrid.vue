<script setup>
/**
 * VxeGrid — 项目内统一的数据网格外壳 (vxe-table)
 *
 * 用法:
 *   <VxeGrid
 *     :data="list" :loading="loading"
 *     :total="total" :page="query.page" :rows="query.rows"
 *     :empty="{ icon, title, desc }"
 *     :height="640"
 *     @page-change="onPageChange"
 *   >
 *     <template #toolbar>
 *       <button class="btn btn-primary" @click="onCreate">新增</button>
 *     </template>
 *     <vxe-column title="名称" field="name" min-width="220">
 *       <template #default="{ row }"> ... </template>
 *     </vxe-column>
 *     <!-- 操作列固定在右侧 -->
 *     <vxe-column title="操作" width="160" fixed="right" align="right">
 *       <template #default="{ row }"> ... </template>
 *     </vxe-column>
 *   </VxeGrid>
 */
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import EmptyState from './EmptyState.vue'
import ScrollIndicator from './ScrollIndicator.vue'

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
  scrollX:     { type: [String, Number], default: 'max-content' },
  rowKey:      { type: String, default: '_rowKey' },
})

const emit = defineEmits(['page-change'])

/* ===== 分页 ===== */
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

/* ===== 横向滚动指示器 ===== */
const tableRef = ref(null)
const scrollLeft = ref(0)
const scrollWidth = ref(0)
const clientWidth = ref(0)
const hasOverflow = ref(false)

function readScroll() {
  const tbl = tableRef.value
  if (!tbl) return
  const $t = tbl.$table || tbl
  const refData = $t && $t.refData ? $t.refData : $t
  const body = ($t && $t.scrollBodyWrapper)
    || (refData && refData.scrollBodyWrapper)
    || (refData && refData.bodyWrapper)
  if (body) {
    scrollLeft.value = body.scrollLeft || 0
    scrollWidth.value = body.scrollWidth || 0
    clientWidth.value = body.clientWidth || 0
    hasOverflow.value = scrollWidth.value > clientWidth.value + 1
  }
}

let unbindFn = null
function bindScrollListener() {
  nextTick(() => {
    const tbl = tableRef.value
    if (!tbl) return
    const $t = tbl.$table || tbl
    const refData = $t && $t.refData ? $t.refData : $t
    const body = ($t && $t.scrollBodyWrapper)
      || (refData && refData.scrollBodyWrapper)
      || (refData && refData.bodyWrapper)
    if (!body) return
    if (body.__vxgBound) {
      readScroll()
      return
    }
    body.__vxgBound = true
    const handler = () => {
      scrollLeft.value = body.scrollLeft || 0
      scrollWidth.value = body.scrollWidth || 0
      clientWidth.value = body.clientWidth || 0
      hasOverflow.value = scrollWidth.value > clientWidth.value + 1
    }
    body.addEventListener('scroll', handler, { passive: true })
    unbindFn = () => body.removeEventListener('scroll', handler)
    handler()
  })
}

onMounted(() => {
  bindScrollListener()
  window.addEventListener('resize', readScroll)
})

onBeforeUnmount(() => {
  if (unbindFn) unbindFn()
  window.removeEventListener('resize', readScroll)
})

watch(() => props.data, () => {
  bindScrollListener()
}, { flush: 'post' })

watch(() => props.loading, (v) => {
  if (!v) bindScrollListener()
})
</script>

<template>
  <div class="vxg">
    <div v-if="$slots.toolbar" class="vxg-toolbar">
      <div class="vxg-toolbar-left">
        <slot name="toolbar" />
      </div>
      <ScrollIndicator
        v-if="hasOverflow"
        :scroll-left="scrollLeft"
        :scroll-width="scrollWidth"
        :client-width="clientWidth"
      />
    </div>

    <vxe-table
      ref="tableRef"
      :data="data"
      :loading="loading"
      :height="height"
      :max-height="maxHeight"
      :size="size"
      :scroll-x="{ enabled: true, gt: 0 }"
      :scroll-y="{ enabled: !!height || !!maxHeight, gt: 60 }"
      :column-config="{ resizable: true, minWidth: 80 }"
      :row-config="{ isHover: true, keyField: rowKey }"
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

    <div v-if="showPager && total > 0" class="vxg-pager">
      <vxe-pager
        v-bind="pagerConfig"
        @page-change="onPageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.vxg {
  width: 100%;
  background: var(--bg-elevated);
  border-radius: var(--radius-xl);
  overflow: hidden;
  border: 1px solid var(--hairline);
  box-shadow: var(--shadow-card);
  transition: all var(--dur-base) var(--ease-glacis);
}

/* ===== Toolbar ===== */
.vxg-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--hairline);
  background: var(--bg-sunken);
  flex-wrap: wrap;
}
.vxg-toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.vxg-table {
  width: 100%;
  font-size: 13.5px;
  color: var(--text-default);
  --vxe-table-header-background-color: var(--bg-sunken);
  --vxe-table-footer-background-color: var(--bg-sunken);
  --vxe-table-row-striped-background-color: var(--bg-sunken);
  --vxe-table-row-hover-background-color: var(--bg-deep);
  --vxe-table-row-current-background-color: var(--info-soft);
  --vxe-table-border-color: var(--hairline);
  --vxe-table-header-text-color: var(--text-muted);
  --vxe-table-resizable-color: var(--primary-300);
  --vxe-table-fixed-column-border-color: var(--hairline);
  --vxe-table-cell-padding-left: 14px;
  --vxe-table-cell-padding-right: 14px;
}

/* Dark 模式下,vxe 的 stripe 默认色 #fafafa 会显示为亮色斑马条,需要主动覆写 */
:root.dark .vxg-table {
  --vxe-table-row-striped-background-color: rgba(255, 255, 255, 0.025);
  --vxe-table-header-background-color: var(--bg-sunken);
  --vxe-table-border-color: var(--hairline);
  --vxe-table-resizable-color: var(--primary-400);
}

/* ===== vxe-table 内置样式覆盖 ===== */
:deep(.vxe-table--render-default .vxe-table--body-wrapper),
:deep(.vxe-table--render-default .vxe-table--header-wrapper) {
  background: transparent;
}

:deep(.vxe-table--header) {
  background: var(--bg-sunken);
}

:deep(.vxe-table--header .vxe-header--column) {
  color: var(--text-muted) !important;
  font-weight: 600 !important;
  font-size: 12.5px !important;
  letter-spacing: 0.02em;
  background: transparent !important;
  height: 44px;
  border-bottom: 1px solid var(--hairline) !important;
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
  background: var(--info-soft) !important;
}

/* 覆盖 vxe 内置的 stripe 偶数行:深色下 #fafafa 太亮 */
:deep(.vxe-table--body .vxe-body--row.row--stripe) {
  background-color: rgba(15, 23, 42, 0.025) !important;
}
:root.dark :deep(.vxe-table--body .vxe-body--row.row--stripe) {
  background-color: rgba(255, 255, 255, 0.025) !important;
}

/* 固定列(fixed 列)在 dark 下需要稍亮的底色以营造层次 */
:deep(.vxe-table .vxe-table--fixed-left-wrapper),
:deep(.vxe-table .vxe-table--fixed-right-wrapper) {
  box-shadow: none;
}
:deep(.vxe-body--column.col--fixed),
:deep(.vxe-header--column.col--fixed) {
  background: inherit !important;
}

/* hover / stripe 时,固定列要跟随所在行的底色,避免撕裂 */
:deep(.vxe-table--body .vxe-body--row:hover .vxe-body--column.col--fixed),
:deep(.vxe-table--body .vxe-body--row:hover .vxe-header--column.col--fixed) {
  background: var(--bg-deep) !important;
}
:deep(.vxe-table--body .vxe-body--row.row--stripe .vxe-body--column.col--fixed),
:deep(.vxe-table--body .vxe-body--row.row--stripe .vxe-header--column.col--fixed) {
  background: rgba(15, 23, 42, 0.025) !important;
}
:root.dark :deep(.vxe-table--body .vxe-body--row.row--stripe .vxe-body--column.col--fixed),
:root.dark :deep(.vxe-table--body .vxe-body--row.row--stripe .vxe-header--column.col--fixed) {
  background: rgba(255, 255, 255, 0.025) !important;
}

/* 拖拽列分隔手柄 */
:deep(.vxe-table .vxe-table--resizable-col) {
  width: 8px;
}
:deep(.vxe-table .vxe-table--resizable-col:hover),
:deep(.vxe-table .col--resize-active) {
  background: var(--primary-300);
  opacity: 0.55;
}
:deep(.vxe-table .vxe-table--resizable-line) {
  background: var(--primary-500);
}

/* 横向滚动条 webkit 美化 */
:deep(.vxe-table .vxe-table--body-wrapper)::-webkit-scrollbar {
  height: 10px;
  width: 10px;
}
:deep(.vxe-table .vxe-table--body-wrapper)::-webkit-scrollbar-track {
  background: var(--bg-sunken);
}
:deep(.vxe-table .vxe-table--body-wrapper)::-webkit-scrollbar-thumb {
  background: var(--hairline-strong);
  border-radius: 999px;
  border: 2px solid var(--bg-sunken);
}
:deep(.vxe-table .vxe-table--body-wrapper)::-webkit-scrollbar-thumb:hover {
  background: var(--primary-300);
}

:deep(.vxe-table--body .vxe-body--column) {
  background: transparent !important;
  color: var(--text-default) !important;
  font-size: 13.5px;
  border-bottom: 1px solid var(--hairline) !important;
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
  background: var(--bg-glass) !important;
  backdrop-filter: blur(2px);
}

:deep(.vxe-table .vxe-loading .vxe-loading--spinner) {
  color: var(--primary-500);
}

:deep(.vxe-table .vxe-loading--text) {
  color: var(--text-default) !important;
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
  background: var(--bg-elevated) !important;
  border: 1px solid var(--hairline) !important;
  color: var(--text-default) !important;
  border-radius: var(--radius-md) !important;
  min-width: 32px;
  height: 32px;
  transition: all var(--dur-base) var(--ease-glacis);
}

:deep(.vxe-pager .vxe-pager--num-btn.is--active) {
  background: var(--primary-500) !important;
  border-color: var(--primary-500) !important;
  color: #ffffff !important;
}

:deep(.vxe-pager .vxe-pager--btn:hover),
:deep(.vxe-pager .vxe-pager--num-btn:hover) {
  border-color: var(--primary-500) !important;
  color: var(--primary-600) !important;
}
:root.dark :deep(.vxe-pager .vxe-pager--btn:hover),
:root.dark :deep(.vxe-pager .vxe-pager--num-btn:hover) {
  color: var(--primary-300) !important;
}

/* Dark 下 pager 容器区分表头 */
:root.dark .vxg-pager {
  background: var(--bg-sunken);
  border-top-color: var(--hairline);
}

/* Dark 下 vxe-pager sizes 下拉 arrow */
:root.dark :deep(.vxe-pager .vxe-pager--sizes .vxe-pager--select .vxe-input--suffix-icon),
:root.dark :deep(.vxe-pager .vxe-pager--jump input) {
  color: var(--text-default);
  background: var(--bg-elevated);
}
:deep(.vxe-pager .vxe-pager--num-btn.is--active:hover) {
  color: #ffffff !important;
}

:deep(.vxe-pager .vxe-pager--sizes .vxe-pager--select) {
  border: 1px solid var(--hairline) !important;
  border-radius: var(--radius-md) !important;
  background: var(--bg-elevated);
}
</style>
