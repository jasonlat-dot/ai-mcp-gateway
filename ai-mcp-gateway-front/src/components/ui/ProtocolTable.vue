<script setup>
/**
 * ProtocolTable — 通用扁平 monospace 协议表面板
 *
 * 设计基线: dashboard「协议接入时间」面板
 *   - 单层 grid 行 + chip + pill
 *   - 无 vxe-grid, 无斑马底
 *   - 每行点击可选, 默认无 hover 反馈(由调用方决定)
 *
 * 行为:
 *   - columns 渲染头部
 *   - rows 渲染主体, 每行由 slot(默认) 提供 cell 渲染
 *   - slot 接收 { row, index }
 *   - 空态: 默认 EmptyState, 也可由调用方覆盖(empty slot)
 */
import EmptyState from './EmptyState.vue'

const props = defineProps({
  columns: { type: Array, default: () => [] },
  rows:    { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  /* 空态配置 */
  empty: {
    type: Object,
    default: () => ({ icon: 'Box', title: '暂无数据', desc: '' }),
  },
  /* 行 key 字段 */
  rowKey: { type: String, default: 'id' },
})

function getRowKey(row, index) {
  if (row && row._rowKey) return row._rowKey
  if (row && row[props.rowKey] != null) return row[props.rowKey]
  return index
}
</script>

<template>
  <div class="protocol-table" :class="{ 'is-empty': !loading && rows.length === 0 }">
    <!-- 表头 -->
    <div
      v-if="columns.length"
      class="pt-head"
      :style="{ gridTemplateColumns: columns.map(c => c.width || '1fr').join(' ') }"
    >
      <span
        v-for="col in columns"
        :key="col.key"
        class="pt-col pt-col-head"
        :class="[`pt-col-${col.key}`, { 'is-right': col.align === 'right', 'is-center': col.align === 'center' }]"
      >
        {{ col.label }}
      </span>
    </div>

    <!-- 加载骨架 -->
    <ul v-if="loading" class="pt-body">
      <li v-for="i in 4" :key="`sk-${i}`" class="pt-row pt-skel">
        <span
          v-for="col in columns"
          :key="`sk-${i}-${col.key}`"
          class="pt-col"
          :class="`pt-col-${col.key}`"
        >
          <span class="sk-line" />
        </span>
      </li>
    </ul>

    <!-- 空态 -->
    <div v-else-if="rows.length === 0" class="empty-host">
      <slot name="empty">
        <EmptyState :icon="empty.icon" :title="empty.title" :desc="empty.desc" />
      </slot>
    </div>

    <!-- 数据行 -->
    <ul v-else class="pt-body">
      <li
        v-for="(row, index) in rows"
        :key="getRowKey(row, index)"
        class="pt-row"
        :style="{ gridTemplateColumns: columns.map(c => c.width || '1fr').join(' ') }"
      >
        <slot :row="row" :index="index" />
      </li>
    </ul>
  </div>
</template>

<style scoped>
.protocol-table {
  display: flex;
  flex-direction: column;
  gap: 4px;
  /* 默认走 Plus Jakarta Sans / Inter (sub2api 主体),单格内部自己覆盖 */
  font-family: 'Plus Jakarta Sans', 'Inter', system-ui, -apple-system,
    BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: var(--fs-sm);
  color: var(--text-default);
}

/* ===== 表头 ===== */
/* 表头: 小写灰, 跟 sub2api .table thead 一致 */
.pt-head {
  display: grid;
  gap: 14px;
  padding: 0 12px 8px;
  border-bottom: 1px solid var(--hairline);
  color: var(--text-subtle);
  font-size: var(--fs-2xs);
  font-weight: var(--fw-semibold);
  letter-spacing: var(--ls-wide);
  text-transform: none;
}

.pt-col-head {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}
.pt-col-head.is-right  { justify-content: flex-end; }
.pt-col-head.is-center { justify-content: center; }

/* ===== 行: 紧凑 padding, hover 走 --bg-deep,跟 .table tbody 一致 ===== */
.pt-body { display: flex; flex-direction: column; }

.pt-row {
  display: grid;
  gap: 14px;
  align-items: center;
  padding: 12px;
  border-radius: var(--radius-md);
  transition: background-color var(--dur-fast) var(--ease-glacis);
  color: var(--text-default);
  min-width: 0;
}
.pt-row + .pt-row { margin-top: 2px; }
.pt-row:hover { background: var(--bg-sunken); }

.pt-col {
  display: flex;
  align-items: center;
  min-width: 0;
  /* 继承父级 sans-serif; 子节点需要 monospace 时显式覆盖 */
}
.pt-col.is-right  { justify-content: flex-end; }
.pt-col.is-center { justify-content: center; }

/* 公共 slot 内容用 slot 内的 .pt-cell-* / .pt-content 即可,
   父级不需要再特殊化 */

/* ===== 骨架 ===== */
.pt-skel .sk-line {
  display: block;
  width: 80%;
  height: 14px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, var(--bg-deep) 0%, var(--bg-sunken) 50%, var(--bg-deep) 100%);
  background-size: 200% 100%;
  animation: shimmer 1.5s linear infinite;
}
.pt-skel { pointer-events: none; }
.pt-skel:hover { background: transparent; }

/* ===== 空态 ===== */
.empty-host {
  padding: 28px 12px;
  display: flex;
  justify-content: center;
}
</style>
