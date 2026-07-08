<script setup>
/**
 * ElGrid — 项目内统一的数据网外壳 (基于 el-table + el-pagination)
 *
 * 用法:
 *   <ElGrid
 *     :data="list" :loading="loading"
 *     :total="total" :page="query.page" :rows="query.rows"
 *     :empty="{ icon: 'Connection', title: '暂无网关', desc: '...' }"
 *     @page-change="onPageChange"
 *   >
 *     <template #toolbar>
 *       <button class="btn btn-secondary" @click="load">刷新</button>
 *     </template>
 *
 *     <el-table-column prop="name" label="名称" min-width="220">
 *       <template #default="{ row }"> ... </template>
 *     </el-table-column>
 *
 *     <el-table-column label="操作" width="170" fixed="right" align="right">
 *       <template #default="{ row }"> ... </template>
 *     </el-table-column>
 *   </ElGrid>
 *
 * 设计要点:
 *  - 列宽可拖拽:自实现 vResizeColumn 指令(避免引入 element-resizable-directive)
 *  - 横向溢出指示器:ScrollIndicator,跟随 .el-table__body-wrapper 的 scrollLeft/scrollWidth/clientWidth
 *  - 主题 token 注入:在组件级覆盖 --el-* CSS 变量,白天/黑夜一致
 */
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import EmptyState from './EmptyState.vue'
import ScrollIndicator from './ScrollIndicator.vue'

const props = defineProps({
  data:        { type: Array,  default: () => [] },
  loading:     { type: Boolean, default: false },
  height:      { type: [String, Number], default: null },
  maxHeight:   { type: [String, Number], default: null },
  showPager:   { type: Boolean, default: true },
  total:       { type: Number, default: 0 },
  page:        { type: Number, default: 1 },
  rows:        { type: Number, default: 10 },
  emptyIcon:   { type: String, default: 'Box' },
  emptyTitle:  { type: String, default: '暂无数据' },
  emptyDesc:   { type: String, default: '' },
  emptyTone:   { type: String, default: 'info' },
  rowKey:      { type: String, default: '_rowKey' },
})

const emit = defineEmits(['page-change'])

function onPage(p) {
  emit('page-change', { page: p, rows: props.rows })
}
function onSize(s) {
  emit('page-change', { page: props.page, rows: s })
}

/* ===== 横向滚动指示器 ===== */
const tableRef = ref(null)
const scrollLeft = ref(0)
const scrollWidth = ref(0)
const clientWidth = ref(0)
const hasOverflow = ref(false)
let bodyWrapper = null
let unbind = null

function readScroll() {
  if (!bodyWrapper) return
  scrollLeft.value = bodyWrapper.scrollLeft || 0
  scrollWidth.value = bodyWrapper.scrollWidth || 0
  clientWidth.value = bodyWrapper.clientWidth || 0
  hasOverflow.value = scrollWidth.value > clientWidth.value + 1
}

function bind() {
  nextTick(() => {
    const tbl = tableRef.value
    if (!tbl || !tbl.$el) return
    bodyWrapper = tbl.$el.querySelector('.el-table__body-wrapper')
    if (!bodyWrapper) return
    if (bodyWrapper.__elgBound) {
      readScroll()
      return
    }
    bodyWrapper.__elgBound = true
    const handler = () => readScroll()
    bodyWrapper.addEventListener('scroll', handler, { passive: true })
    unbind = () => {
      bodyWrapper.removeEventListener('scroll', handler)
      bodyWrapper.__elgBound = false
    }
    readScroll()
  })
}

onMounted(() => {
  bind()
  window.addEventListener('resize', readScroll)
})

onBeforeUnmount(() => {
  if (unbind) unbind()
  window.removeEventListener('resize', readScroll)
})

watch(() => props.data, () => bind(), { flush: 'post' })
watch(() => props.loading, (v) => { if (!v) bind() })

/* ===== 自实现列宽拖拽指令 vResizeColumn =====
 *
 * 作用: 给 <el-table> 加上 v-resize-column 后,所有 <el-table-column> 都可以鼠标拖拽列宽。
 * 实现: 监听表头每个 cell,在右侧 6px 区域按下鼠标后,移动鼠标改变该列 width。
 */
const vResizeColumn = {
  mounted(el, binding) {
    applyResize(el, binding?.instance)
  },
  updated(el, binding) {
    // 重新扫描:每次数据/列变化时,th 节点重建,需要重新绑定
    el.__elgResizeBound = false
    applyResize(el, binding?.instance)
  },
}

function applyResize(tableEl, instance) {
  if (!tableEl || tableEl.__elgResizeBound) return
  const headerWrapper = tableEl.querySelector('.el-table__header-wrapper')
  if (!headerWrapper) return

  const tr = headerWrapper.querySelector('tr')
  if (!tr) return

  // 拿到 el-table 实例(优先 binding.instance,其次通过 __vueParentComponent 向上找)
  const tblInstance = instance || tableEl.__vueParentComponent?.proxy

  const ths = Array.from(tr.children)
  ths.forEach((th) => {
    th.style.position = th.style.position || 'relative'
    if (th.querySelector(':scope > .elg-resize-handle')) return
    const handle = document.createElement('span')
    handle.className = 'elg-resize-handle'
    handle.setAttribute('aria-hidden', 'true')
    th.appendChild(handle)

    const onMouseDown = (e) => {
      e.preventDefault()
      e.stopPropagation()
      const startX = e.clientX
      const rect = th.getBoundingClientRect()
      const startW = rect.width

      const colKey = th.getAttribute('data-col-key')
      // 通过实例拿到 store;ElTable 内部 store.states.columns 是响应式数组
      const store = tblInstance?.store || tblInstance?.layout?.store
      const colsRef = store?.states?.columns
      const columns = (colsRef && colsRef.value) || store?.states?.columns || []
      const col = columns.find((c) => c && (c.id === colKey || c.columnKey === colKey || c.property === colKey))
      if (!col) return

      document.body.style.cursor = 'col-resize'
      const onMove = (ev) => {
        const next = Math.max(60, startW + (ev.clientX - startX))
        // 1. 改内部 store 列宽
        col.width = next > 0 ? `${next}px` : undefined
        col.realWidth = next
        // 2. 同步 th 宽度
        th.style.width = `${next}px`
        // 3. 同步 body 单元格的同列
        const bodyWrapper2 = tableEl.querySelector('.el-table__body-wrapper')
        if (bodyWrapper2) {
          bodyWrapper2.querySelectorAll(`td.${colKey}`).forEach((td) => {
            td.style.width = `${next}px`
          })
        }
        // 4. 触发 el-table 重排布局
        store?.scheduleLayout?.()
      }
      const onUp = () => {
        document.body.style.cursor = ''
        window.removeEventListener('mousemove', onMove)
        window.removeEventListener('mouseup', onUp)
        store?.updateKey?.()
      }
      window.addEventListener('mousemove', onMove)
      window.addEventListener('mouseup', onUp)
    }

    handle.addEventListener('mousedown', onMouseDown)
  })

  tableEl.__elgResizeBound = true
}
</script>

<template>
  <div class="elg">
    <div class="elg-toolbar">
      <div v-if="$slots.toolbar" class="elg-toolbar-left">
        <slot name="toolbar" />
      </div>
      <ScrollIndicator
        v-if="hasOverflow"
        :scroll-left="scrollLeft"
        :scroll-width="scrollWidth"
        :client-width="clientWidth"
      />
    </div>

    <el-table
      ref="tableRef"
      :data="data"
      :height="height"
      :max-height="maxHeight"
      :row-key="rowKey"
      stripe
      border
      show-overflow-tooltip
      class="elg-table"
      v-resize-column
    >
      <template #empty>
        <EmptyState :icon="emptyIcon" :title="emptyTitle" :desc="emptyDesc" :tone="emptyTone" />
      </template>
      <slot />
    </el-table>

    <div v-if="showPager && total > 0" class="elg-pager">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :current-page="page"
        :page-size="rows"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="onPage"
        @size-change="onSize"
      />
    </div>
  </div>
</template>

<style scoped>
.elg {
  width: 100%;
  background: var(--bg-elevated);
  border-radius: var(--radius-xl);
  overflow: hidden;
  border: 1px solid var(--hairline);
  box-shadow: var(--shadow-card);
  transition: all var(--dur-base) var(--ease-glacis);
}

/* ===== Toolbar ===== */
.elg-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--hairline);
  background: var(--bg-sunken);
  flex-wrap: wrap;
}
.elg-toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* ===== el-table 主题适配 ===== */
.elg-table {
  width: 100%;
  font-size: 13.5px;
  color: var(--text-default);
  --el-table-border-color: var(--hairline);
  --el-table-header-bg-color: var(--bg-sunken);
  --el-table-header-text-color: var(--text-muted);
  --el-table-row-hover-bg-color: var(--bg-deep);
  --el-table-striped-bg-color: var(--bg-sunken);
  --el-table-bg-color: var(--bg-elevated);
  --el-table-fixed-box-shadow: -6px 0 8px -4px rgba(15, 23, 42, 0.10);
}

:root.dark .elg-table {
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.04);
  --el-table-striped-bg-color: rgba(255, 255, 255, 0.025);
  --el-table-fixed-box-shadow: -6px 0 8px -4px rgba(0, 0, 0, 0.40);
}

/* ===== Header ===== */
:deep(.elg-table .el-table__header-wrapper th) {
  background: var(--bg-sunken);
  color: var(--text-muted) !important;
  font-weight: 600 !important;
  font-size: 12.5px !important;
  letter-spacing: 0.02em;
  border-bottom: 1px solid var(--hairline) !important;
}
:deep(.elg-table .el-table__header-wrapper th .cell) {
  padding: 0 14px;
}

/* ===== Row hover / stripe ===== */
:deep(.elg-table .el-table__row) {
  transition: background var(--dur-base) var(--ease-glacis);
}
:deep(.elg-table .el-table__row:hover > td) {
  background: var(--bg-deep) !important;
}
:deep(.elg-table .el-table__row.el-table__row--striped td) {
  background: var(--bg-sunken) !important;
}
:root.dark :deep(.elg-table .el-table__row.el-table__row--striped td) {
  background: rgba(255, 255, 255, 0.025) !important;
}

/* fixed 列 hover / stripe 跟随所在行,避免撕裂 */
:deep(.elg-table .el-table__row:hover td.is-fixed-left),
:deep(.elg-table .el-table__row:hover td.is-fixed-right) {
  background: var(--bg-deep) !important;
}
:deep(.elg-table .el-table__row.el-table__row--striped td.is-fixed-left),
:deep(.elg-table .el-table__row.el-table__row--striped td.is-fixed-right) {
  background: var(--bg-sunken) !important;
}
:root.dark :deep(.elg-table .el-table__row.el-table__row--striped td.is-fixed-left),
:root.dark :deep(.elg-table .el-table__row.el-table__row--striped td.is-fixed-right) {
  background: rgba(255, 255, 255, 0.025) !important;
}

/* ===== Body cell ===== */
:deep(.elg-table td.el-table__cell) {
  background: var(--bg-elevated) !important;
  color: var(--text-default);
  border-bottom: 1px solid var(--hairline) !important;
  padding: 10px 0;
}
:deep(.elg-table td.el-table__cell .cell) {
  padding: 0 14px;
  line-height: 1.5;
  font-size: 13.5px;
}

/* ===== 列宽拖拽手柄 ===== */
:deep(.elg-table .el-table__header-wrapper th .elg-resize-handle) {
  position: absolute;
  top: 0;
  right: 0;
  width: 6px;
  height: 100%;
  cursor: col-resize;
  user-select: none;
  z-index: 2;
}
:deep(.elg-table .el-table__header-wrapper th .elg-resize-handle::before) {
  content: '';
  position: absolute;
  top: 25%;
  bottom: 25%;
  left: 50%;
  width: 2px;
  border-radius: 1px;
  background: transparent;
  transition: background var(--dur-fast) var(--ease-glacis);
}
:deep(.elg-table .el-table__header-wrapper th:hover .elg-resize-handle::before) {
  background: var(--primary-400);
}
:root.dark :deep(.elg-table .el-table__header-wrapper th:hover .elg-resize-handle::before) {
  background: var(--primary-300);
}

/* ===== 横向滚动条 webkit 美化 ===== */
:deep(.elg-table .el-table__body-wrapper)::-webkit-scrollbar {
  height: 10px;
  width: 10px;
}
:deep(.elg-table .el-table__body-wrapper)::-webkit-scrollbar-track {
  background: var(--bg-sunken);
}
:deep(.elg-table .el-table__body-wrapper)::-webkit-scrollbar-thumb {
  background: var(--hairline-strong);
  border-radius: 999px;
  border: 2px solid var(--bg-sunken);
}
:deep(.elg-table .el-table__body-wrapper)::-webkit-scrollbar-thumb:hover {
  background: var(--primary-300);
}

/* ===== Pager ===== */
.elg-pager {
  padding: 14px 16px;
  border-top: 1px solid var(--hairline);
  background: var(--bg-sunken);
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

:deep(.elg-pager .el-pagination) {
  --el-pagination-bg-color: var(--bg-elevated);
  --el-pagination-button-color: var(--text-default);
  --el-pagination-button-disabled-bg-color: transparent;
  --el-pagination-hover-color: var(--primary-600);
  color: var(--text-muted);
  font-weight: 500;
  font-size: 13px;
}
:deep(.elg-pager .el-pager li),
:deep(.elg-pager .el-pagination .btn-prev),
:deep(.elg-pager .el-pagination .btn-next) {
  background: var(--bg-elevated) !important;
  border: 1px solid var(--hairline) !important;
  color: var(--text-default) !important;
  border-radius: var(--radius-md) !important;
  min-width: 32px;
  height: 32px;
  line-height: 30px !important;
  font-weight: 500;
  transition: all var(--dur-base) var(--ease-glacis);
}
:deep(.elg-pager .el-pager li.is-active) {
  background: var(--primary-500) !important;
  border-color: var(--primary-500) !important;
  color: #ffffff !important;
}
:deep(.elg-pager .el-pager li:hover),
:deep(.elg-pager .el-pagination .btn-prev:hover),
:deep(.elg-pager .el-pagination .btn-next:hover) {
  border-color: var(--primary-500) !important;
  color: var(--primary-600) !important;
}
:root.dark :deep(.elg-pager .el-pager li:hover),
:root.dark :deep(.elg-pager .el-pagination .btn-prev:hover),
:root.dark :deep(.elg-pager .el-pagination .btn-next:hover) {
  color: var(--primary-300) !important;
}
:root.dark .elg-pager {
  background: var(--bg-sunken);
  border-top-color: var(--hairline);
}
:root.dark :deep(.elg-pager .el-pagination .el-pagination__sizes .el-select .el-input__inner) {
  color: var(--text-default);
  background: var(--bg-elevated);
  border-color: var(--hairline);
}
:root.dark :deep(.elg-pager .el-pagination .el-pagination__jump) {
  color: var(--text-muted);
}
:root.dark :deep(.elg-pager .el-pagination .el-pagination__jump .el-input__inner) {
  color: var(--text-default);
  background: var(--bg-elevated);
  border-color: var(--hairline);
}
</style>