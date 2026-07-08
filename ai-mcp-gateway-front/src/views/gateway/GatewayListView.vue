<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import IdCell from '@/components/ui/IdCell.vue'
import ElGrid from '@/components/ui/ElGrid.vue'
import ToggleSegments from '@/components/ui/ToggleSegments.vue'
import {
  pageGatewayConfig,
  saveGatewayConfig,
  updateGatewayConfig,
  deleteGatewayConfig,
  buildSseUrl,
  copyText,
  listGatewayToolByGatewayId,
  listGatewayAuth,
} from '@/api/admin'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import { authBadge, statusBadge } from '@/utils/format'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ gatewayId: '', gatewayName: '', page: 1, rows: 10 })

const dialog = ref(false)
const submitting = ref(false)
const editMode = ref('create')
const form = reactive(resetForm())

function resetForm() {
  return {
    gatewayId: '',
    gatewayName: '',
    gatewayDesc: '',
    version: '1.0.0',
    auth: 1,
    status: 1,
  }
}

async function load() {
  loading.value = true
  try {
    const { list: data, total: t } = await pageGatewayConfig({
      gatewayId:   query.gatewayId || undefined,
      gatewayName: query.gatewayName || undefined,
      page: query.page,
      rows: query.rows,
    })
    list.value = (data || []).map((r, i) => ({ ...r, _rowKey: `${r.gatewayId}-${i}` }))
    total.value = t || 0
  } catch (err) {
    list.value = []
    total.value = 0
    toast.error(err?.message || '加载列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

function onSearch() {
  query.page = 1
  load()
}

function onReset() {
  query.gatewayId = ''
  query.gatewayName = ''
  query.page = 1
  load()
}

function openCreate() {
  Object.assign(form, resetForm())
  editMode.value = 'create'
  dialog.value = true
}

function openEdit(row) {
  // 后端字段可能是数字 / 字符串 / 布尔 (0/1、"0"/"1"、true/false),统一强转 Number,
  // 这样 ToggleSegments v-model 严格 === 比较时一定能命中某个选项并高亮
  const toBool01 = (v) => {
    const n = Number(v)
    return n === 0 || v === '0' || v === false ? 0 : 1
  }
  Object.assign(form, {
    gatewayId:   row.gatewayId ?? '',
    gatewayName: row.gatewayName ?? '',
    gatewayDesc: row.gatewayDesc ?? '',
    version:     row.version || '1.0.0',
    auth:   toBool01(row.auth),
    status: toBool01(row.status),
  })
  editMode.value = 'edit'
  dialog.value = true
}

async function onSubmit() {
  if (!form.gatewayId) return toast.warning('请填写网关 ID')
  if (!form.gatewayName) return toast.warning('请填写网关名称')
  submitting.value = true
  try {
    const payload = {
      gatewayId:   form.gatewayId,
      gatewayName: form.gatewayName,
      gatewayDesc: form.gatewayDesc,
      version:     form.version || '1.0.0',
      auth:   Number(form.auth),
      status: Number(form.status),
    }
    if (editMode.value === 'create') {
      await saveGatewayConfig(payload)
      toast.success('网关创建成功', { duration: 1800 })
    } else {
      await updateGatewayConfig(payload)
      toast.success('网关已更新', { duration: 1800 })
    }
    dialog.value = false
    load()
  } catch (err) {
    toast.error(err?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function copySse(row) {
  try {
    await copyText(buildSseUrl(row.gatewayId))
    toast.success('SSE 地址已复制', { duration: 1800 })
  } catch {
    toast.error('复制失败', { duration: 1800 })
  }
}

function gotoDetail(row) {
  router.push(`/gateways/${row.gatewayId}`)
}

/* ==== 删除流程: 先检查引用, 再让用户确认 ==== */
const pendingDelete = ref(null)       // 待删网关行
const deleteCheck    = ref(null)      // { loading, tools:[], auths:[], error?: string }

/** confirm 的副标题:动态展示网关身份信息 */
const dialogDesc = computed(() => {
  const row = pendingDelete.value
  if (!row) return ''
  return `确认要删除网关 ${row.gatewayId} (${row.gatewayName || '未命名'}) 吗?此操作不可恢复。`
})

/** confirm 的 ok 按钮文案:有引用时显示禁用文案 */
const okText = computed(() => {
  if (!deleteCheck.value || deleteCheck.value.loading) return '查询中...'
  return hasReferences(deleteCheck.value) ? '仍有引用,无法删除' : '确认删除'
})

/** 是否有引用(用于禁用确认按钮) */
function hasReferences(check) {
  return check && (check.tools.length > 0 || check.auths.length > 0)
}

/**
 * 点击「删除」:
 *  1. 先异步查 tool 数 + auth 数(用 list 接口并行)
 *  2. 把结果写入 deleteCheck, 等用户二次确认 / 取消
 *
 *  把"检查"和"确认"分两步,让用户清楚删一个网关会带走/打断哪些东西
 */
function openDeleteCheck(row) {
  pendingDelete.value = row
  deleteCheck.value   = { loading: true, tools: [], auths: [], error: '' }
  const gwId = row.gatewayId
  Promise.allSettled([
    listGatewayToolByGatewayId(gwId),
    listGatewayAuth(),
  ]).then(([toolRes, authRes]) => {
    const next = { loading: false, tools: [], auths: [], error: '' }
    if (toolRes.status === 'fulfilled') {
      next.tools = Array.isArray(toolRes.value) ? toolRes.value : []
    } else {
      next.error = (toolRes.reason?.message) || '查询工具列表失败'
    }
    if (authRes.status === 'fulfilled') {
      const allAuths = Array.isArray(authRes.value) ? authRes.value : []
      // auth 没有 byId 端点,只能在客户端按 gatewayId 过滤
      next.auths = allAuths.filter((a) => a.gatewayId === gwId)
    } else if (!next.error) {
      next.error = (authRes.reason?.message) || '查询认证列表失败'
    }
    deleteCheck.value = next
  })
}

/**
 * 点击删除: 先异步查 tool / auth 引用, 写 deleteCheck, 由用户二次确认
 */
async function doDelete() {
  if (!pendingDelete.value) return
  const id = pendingDelete.value.gatewayId
  try {
    await deleteGatewayConfig(id)
    toast.success(`已删除网关 ${id}`, { duration: 1800 })
  } catch (err) {
    toast.error(err?.message || '删除失败')
  } finally {
    pendingDelete.value = null
    deleteCheck.value   = null
    load()
  }
}

function cancelDelete() {
  pendingDelete.value = null
  deleteCheck.value   = null
}

function onPageChange({ page, rows }) {
  query.page = page
  query.rows = rows
  load()
}
</script>

<template>
  <div class="gateway-list">
    <PageCard eyebrow="Gateway Inventory" title="网关列表" desc="管理接入的每一个 AI 网关、它们的状态与认证策略">
      <div class="filter">
        <div class="field">
          <label>网关 ID</label>
          <input v-model="query.gatewayId" type="text" class="inp" placeholder="例如 gw-001" @keyup.enter="onSearch" />
        </div>
        <div class="field">
          <label>网关名称</label>
          <input v-model="query.gatewayName" type="text" class="inp" placeholder="例如 核心业务网关" @keyup.enter="onSearch" />
        </div>
        <div class="actions">
          <button class="btn btn-ghost" @click="onReset"><el-icon><RefreshLeft /></el-icon> 重置</button>
          <button class="btn btn-primary" @click="onSearch"><el-icon><Search /></el-icon> 查询</button>
        </div>
        <div class="toolbar">
          <button class="btn btn-secondary" @click="load">
            <el-icon><Refresh /></el-icon> 刷新
          </button>
          <button class="btn btn-primary" @click="openCreate">
            <el-icon><Plus /></el-icon> 新增网关
          </button>
        </div>
      </div>

      <ElGrid
        :data="list"
        :loading="loading"
        :total="total"
        :page="query.page"
        :rows="query.rows"
        empty-icon="Connection"
        empty-title="暂无网关"
        empty-desc="点击右上角「新增网关」开始接入,或者尝试重置筛选条件"
        @page-change="onPageChange"
      >
        <el-table-column prop="gatewayName" label="名称" min-width="220">
          <template #default="{ row }">
            <div class="gw-cell">
              <div class="gw-avatar">{{ (row.gatewayName || row.gatewayId || '?').slice(0, 1).toUpperCase() }}</div>
              <div class="gw-name" :title="row.gatewayName || '—'">{{ row.gatewayName || '—' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="gatewayDesc" label="描述" min-width="200">
          <template #default="{ row }">
            <span class="muted-desc" :title="row.gatewayDesc || '— 暂无描述 —'">{{ row.gatewayDesc || '— 暂无描述 —' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="gatewayId" label="ID" min-width="200">
          <template #default="{ row }">
            <IdCell :value="row.gatewayId" :max="40" />
          </template>
        </el-table-column>

        <el-table-column prop="version" label="版本" width="100" align="center">
          <template #default="{ row }">
            <span class="ver-pill">v{{ row.version || '1.0.0' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="auth" label="认证" width="120">
          <template #default="{ row }">
            <StatusPill :tone="authBadge(row.auth).tone">
              <el-icon class="dot"><component :is="row.auth === 1 ? 'Lock' : 'Unlock'" /></el-icon>
              {{ authBadge(row.auth).label }}
            </StatusPill>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <StatusPill :tone="statusBadge(row.status).tone" dot>
              {{ statusBadge(row.status).label }}
            </StatusPill>
          </template>
        </el-table-column>

        <el-table-column label="SSE" width="120" align="center">
          <template #default="{ row }">
            <el-tooltip placement="top" :raw-content="true" :show-after="100">
              <template #content>复制 SSE URL</template>
              <button class="btn btn-ghost btn-sm sse-btn" @click.stop="copySse(row)">
                <el-icon><CopyDocument /></el-icon> 复制
              </button>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right" align="right">
          <template #default="{ row }">
            <div class="ops">
              <button class="op-btn" @click="gotoDetail(row)">详情</button>
              <button class="op-btn" @click="openEdit(row)">编辑</button>
              <button class="op-btn op-danger" @click="openDeleteCheck(row)">删除</button>
            </div>
          </template>
        </el-table-column>
      </ElGrid>
    </PageCard>

    <el-dialog v-model="dialog" width="600px" align-center
      :title="editMode === 'create' ? '新增网关基础配置' : '修改网关基础配置'"
      :show-close="false">
      <div class="form-grid">
        <div class="form-item span-2">
          <label>网关 ID</label>
          <input v-model="form.gatewayId" type="text" class="inp" placeholder="例如 gw-001" :readonly="editMode==='edit'" />
        </div>
        <div class="form-item span-2">
          <label>网关名称</label>
          <input v-model="form.gatewayName" type="text" class="inp" placeholder="例如 核心业务网关" />
        </div>
        <div class="form-item span-2">
          <label>网关描述</label>
          <textarea v-model="form.gatewayDesc" class="inp ta" rows="3" placeholder="一段话描述该网关的用途、归属团队等" />
        </div>
        <div class="form-item">
          <label>版本号</label>
          <input v-model="form.version" type="text" class="inp" placeholder="1.0.0" />
        </div>
        <div class="form-item">
          <label>认证强度</label>
          <ToggleSegments
            v-model="form.auth"
            :options="[
              { value: 1, label: '强校验', tone: 'success' },
              { value: 0, label: '不校验', tone: 'warning' },
            ]"
          />
        </div>
        <div class="form-item span-2">
          <label>网关状态</label>
          <ToggleSegments
            v-model="form.status"
            :options="[
              { value: 1, label: '启用', tone: 'success' },
              { value: 0, label: '禁用', tone: 'disabled' },
            ]"
          />
        </div>
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="dialog = false">取消</button>
        <button class="btn btn-primary" :disabled="submitting" @click="onSubmit">
          <el-icon v-if="!submitting"><Check /></el-icon>
          <span v-else class="loading"><span class="spinner" /></span>
          保存配置
        </button>
      </template>
    </el-dialog>

    <ConfirmDialog
      v-if="pendingDelete"
      title="删除网关"
      :desc="dialogDesc"
      :ok-text="okText"
      :ok-disabled="hasReferences(deleteCheck) || (deleteCheck && deleteCheck.loading)"
      tone="danger"
      @confirm="doDelete"
      @cancel="cancelDelete"
    >
      <!-- 工具清单 -->
      <div v-if="deleteCheck && !deleteCheck.loading" class="ref-block">
        <div class="ref-block-head">
          <span class="ref-block-title">已配置的工具</span>
          <span class="ref-block-count" :class="{ 'is-block': deleteCheck.tools.length > 0 }">
            {{ deleteCheck.tools.length }} 个
          </span>
        </div>
        <ul v-if="deleteCheck.tools.length" class="ref-list">
          <li v-for="t in deleteCheck.tools" :key="t.toolId || t.id || t.toolName" class="ref-item">
            <code class="ref-code">{{ t.toolId || t.id || '—' }}</code>
            <span class="ref-name">{{ t.toolName || t.name || '未命名工具' }}</span>
          </li>
        </ul>
        <div v-else class="ref-empty">无</div>

        <div class="ref-block-head" style="margin-top: 14px;">
          <span class="ref-block-title">已配置的 API Key</span>
          <span class="ref-block-count" :class="{ 'is-block': deleteCheck.auths.length > 0 }">
            {{ deleteCheck.auths.length }} 个
          </span>
        </div>
        <ul v-if="deleteCheck.auths.length" class="ref-list">
          <li v-for="a in deleteCheck.auths" :key="a.apiKey || a.id" class="ref-item">
            <code class="ref-code">{{ a.apiKey || a.id || '—' }}</code>
            <span class="ref-name">
              限流 {{ a.rateLimit == null ? '不限' : `${a.rateLimit} 次/小时` }}
            </span>
          </li>
        </ul>
        <div v-else class="ref-empty">无</div>

        <div v-if="deleteCheck.error" class="ref-error">参考信息加载失败: {{ deleteCheck.error }}</div>

        <p v-if="hasReferences(deleteCheck)" class="ref-hint">
          请先到「工具管理」「认证管理」删除或转移以上引用项,再回来删除此网关。
        </p>
      </div>
      <div v-else class="ref-loading">正在查询引用情况...</div>
    </ConfirmDialog>
  </div>
</template>

<style scoped>
.gateway-list { width: 100%; }

.filter {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: 12px;
  margin-bottom: 18px;
}

.field { display: flex; flex-direction: column; gap: 6px; min-width: 200px; max-width: 280px; }
.field label {
  font-size: var(--fs-sm);
  font-weight: var(--fw-medium);
  color: var(--text-default);
}

.actions { display: flex; gap: 8px; align-items: center; height: 40px; }

.toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  height: 40px;
  margin-left: auto;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.form-item { display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.form-item.span-2 { grid-column: 1 / -1; }
.form-item label {
  font-size: var(--fs-sm);
  font-weight: var(--fw-medium);
  color: var(--text-default);
}

.loading { display: inline-flex; align-items: center; }
.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 720ms linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ===== Name cell (avatar + name) ===== */
:deep(.gw-cell) {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
:deep(.gw-avatar) {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  display: grid;
  place-items: center;
  background: var(--gradient-primary-soft);
  border: 1px solid var(--info-line);
  color: var(--primary-600);
  font-weight: var(--fw-bold);
  font-size: var(--fs-sm);
  flex-shrink: 0;
  font-family: 'JetBrains Mono', monospace;
}
:root.dark :deep(.gw-avatar) { color: var(--primary-300); }
:deep(.gw-name) {
  font-weight: var(--fw-semibold);
  color: var(--text-strong);
  font-size: var(--fs-base);
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  letter-spacing: var(--ls-snug);
}

/* ===== Description cell (muted, sans) ===== */
:deep(.muted-desc) {
  display: block;
  color: var(--text-muted);
  font-size: var(--fs-sm);
  line-height: 1.45;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
}

/* ===== Version pill ===== */
:deep(.ver-pill) {
  display: inline-flex;
  align-items: center;
  font-family: 'JetBrains Mono', monospace;
  font-size: var(--fs-2xs);
  font-weight: var(--fw-medium);
  letter-spacing: var(--ls-wide);
  color: var(--text-muted);
  background: var(--bg-deep);
  border: 1px solid var(--hairline);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

/* ===== Pill dot/leading icon ===== */
:deep(.pill .dot) {
  font-size: 11px;
  margin-right: 2px;
}

/* ===== SSE button ===== */
:deep(.sse-btn) {
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  font-size: var(--fs-xs);
  gap: 4px;
  height: 28px;
  padding: 0 10px;
}

/* ===== icon buttons (操作) ===== */
:deep(.icon-btn) {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
}
:deep(.icon-btn:hover) {
  background: var(--bg-deep);
  color: var(--primary-600);
  border-color: transparent;
  transform: none;
}
:root.dark :deep(.icon-btn:hover) { color: var(--primary-300); }

:deep(.ops) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  justify-content: flex-end;
}
.op-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--hairline-strong);
  background: transparent;
  font-family: inherit;
  font-size: var(--fs-xs);
  font-weight: var(--fw-medium);
  color: var(--text-default);
  cursor: pointer;
  transition:
    color var(--dur-base) var(--ease),
    border-color var(--dur-base) var(--ease),
    background-color var(--dur-base) var(--ease),
    transform 220ms var(--ease-spring),
    box-shadow 220ms var(--ease);
  white-space: nowrap;
}
.op-btn:hover {
  color: var(--primary-600);
  border-color: var(--primary-300);
  background: var(--primary-100);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(20, 184, 166, 0.15);
}
.op-btn.op-danger {
  color: var(--text-muted);
}
.op-btn.op-danger:hover {
  color: var(--err-color);
  border-color: rgba(239, 68, 68, 0.45);
  background: var(--err-soft);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.15);
}
:root.dark .op-btn { border-color: var(--hairline-soft); color: var(--text-muted); }
:root.dark .op-btn:hover {
  color: var(--primary-300);
  border-color: rgba(20, 184, 166, 0.45);
  background: rgba(20, 184, 166, 0.10);
  box-shadow: 0 2px 8px rgba(20, 184, 166, 0.20);
}
:root.dark .op-btn.op-danger:hover {
  color: #f87171;
  border-color: rgba(239, 68, 68, 0.45);
  background: rgba(239, 68, 68, 0.12);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.20);
}

/* ===== 删除确认框内的「引用清单」 ===== */
.ref-block {
  width: 100%;
  margin-top: 4px;
  text-align: left;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-md);
  padding: 10px 12px;
}
.ref-block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  font-weight: var(--fw-semibold);
  color: var(--text-muted);
  letter-spacing: 0.02em;
}
.ref-block-title { color: var(--text-muted); }
.ref-block-count {
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--info-soft);
  color: var(--primary-700);
  font-size: 11px;
  font-weight: var(--fw-medium);
  border: 1px solid var(--info-line);
}
.ref-block-count.is-block {
  background: var(--err-soft);
  color: var(--err-color);
  border-color: var(--err-line);
}
:root.dark .ref-block-count { background: rgba(20, 184, 166, 0.12); color: var(--primary-300); border-color: rgba(20, 184, 166, 0.25); }
:root.dark .ref-block-count.is-block { background: rgba(239, 68, 68, 0.12); color: #fca5a5; border-color: rgba(239, 68, 68, 0.30); }

.ref-list {
  list-style: none;
  margin: 6px 0 0;
  padding: 0;
  max-height: 180px;
  overflow-y: auto;
}
.ref-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  border-top: 1px dashed var(--hairline);
  font-size: var(--fs-sm);
}
.ref-item:first-child { border-top: 0; }
.ref-code {
  font-family: var(--font-mono);
  font-size: 11.5px;
  color: var(--text-default);
  background: var(--bg-elevated);
  border: 1px solid var(--hairline);
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ref-name { color: var(--text-muted); }
.ref-empty {
  margin-top: 6px;
  font-size: var(--fs-sm);
  color: var(--text-faint);
}
.ref-error {
  margin-top: 8px;
  font-size: 12px;
  color: var(--warn-color);
  background: var(--warn-soft);
  border: 1px solid var(--warn-line);
  border-radius: var(--radius-sm);
  padding: 6px 8px;
}
.ref-hint {
  margin: 10px 0 0;
  font-size: var(--fs-sm);
  color: var(--err-color);
  background: var(--err-soft);
  border: 1px solid var(--err-line);
  border-radius: var(--radius-sm);
  padding: 8px 10px;
  line-height: 1.55;
}
.ref-loading {
  padding: 18px 0 4px;
  color: var(--text-faint);
  font-size: var(--fs-sm);
}
</style>