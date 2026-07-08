<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import IdCell from '@/components/ui/IdCell.vue'
import ElGrid from '@/components/ui/ElGrid.vue'
import {
  pageGatewayTool,
  listGatewayConfig,
  listGatewayProtocol,
  saveGatewayTool,
  updateGatewayTool,
  deleteGatewayTool,
} from '@/api/admin'
import { useToast } from '@/composables/useToast'

const toast = useToast()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const gateways = ref([])
const protocols = ref([])

const query = reactive({ gatewayId: '', toolId: '', page: 1, rows: 10 })
const dialog = ref(false)
const submitting = ref(false)
const editMode = ref('create')
const form = reactive(resetForm())

function resetForm() {
  return {
    gatewayId: '', toolId: null, toolName: '', toolType: 'function',
    toolDescription: '', toolVersion: '1.0.0',
    protocolId: null, protocolType: 'http',
  }
}
function genId() { return Math.floor(10_000_000 + Math.random() * 90_000_000) }

async function load() {
  loading.value = true
  try {
    const { list: data, total: t } = await pageGatewayTool({
      gatewayId: query.gatewayId || undefined,
      toolId:    query.toolId    || undefined,
      page: query.page,
      rows: query.rows,
    })
    list.value = (data || []).map((r, i) => ({ ...r, _rowKey: `${r.gatewayId}-${r.toolId}-${i}` }))
    total.value = t || 0
  } catch (err) {
    list.value = []
    total.value = 0
    toast.error(err?.message || '加载列表失败')
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const settled = await Promise.allSettled([listGatewayConfig(), listGatewayProtocol()])
  gateways.value  = settled[0].status === 'fulfilled' ? settled[0].value : []
  protocols.value = settled[1].status === 'fulfilled' ? settled[1].value : []
}

onMounted(async () => { await load(); await loadOptions() })

function onSearch() { query.page = 1; load() }
function onReset()  { query.gatewayId = ''; query.toolId = ''; query.page = 1; load() }

function openCreate() {
  Object.assign(form, resetForm())
  form.toolId = genId()
  editMode.value = 'create'
  dialog.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  editMode.value = 'edit'
  dialog.value = true
}

async function onSubmit() {
  if (!form.gatewayId) return toast.warning('请选择网关')
  if (!form.toolName)  return toast.warning('请填写工具名称')
  submitting.value = true
  try {
    const payload = {
      gatewayId:        form.gatewayId,
      toolId:           form.toolId ? Number(form.toolId) : null,
      toolName:         form.toolName,
      toolType:         form.toolType,
      toolDescription:  form.toolDescription,
      toolVersion:      form.toolVersion,
      protocolId:       form.protocolId || null,
      protocolType:     form.protocolType,
    }
    if (editMode.value === 'create') {
      await saveGatewayTool(payload)
      toast.success('工具创建成功', { duration: 1800 })
    } else {
      await updateGatewayTool(payload)
      toast.success('工具已更新', { duration: 1800 })
    }
    dialog.value = false
    load()
  } catch (err) {
    toast.error(err?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const pendingDelete = ref(null)
async function doDelete() {
  if (!pendingDelete.value) return
  try {
    await deleteGatewayTool(pendingDelete.value.gatewayId, pendingDelete.value.toolId)
    toast.success('工具已删除', { duration: 1800 })
    pendingDelete.value = null
    load()
  } catch (err) {
    toast.error(err?.message || '删除失败')
  }
}

function onPageChange({ page, rows }) { query.page = page; query.rows = rows; load() }
const selectedProtocol = computed(() => protocols.value.find(p => p.protocolId === form.protocolId))
</script>

<template>
  <div class="tool-page">
    <PageCard eyebrow="Tool Inventory" title="网关工具管理" desc="为每个网关绑定若干 MCP 工具,每个工具可关联一条 HTTP 协议">
      <div class="filter">
        <div class="field">
          <label>网关 ID</label>
          <input v-model="query.gatewayId" type="text" class="inp" placeholder="例如 gw-001" @keyup.enter="onSearch" />
        </div>
        <div class="field">
          <label>工具 ID</label>
          <input v-model="query.toolId" type="number" class="inp" placeholder="例如 12345678" @keyup.enter="onSearch" />
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
            <el-icon><Plus /></el-icon> 新增工具
          </button>
        </div>
      </div>
<ElGrid
        :data="list"
        :loading="loading"
        :total="total"
        :page="query.page"
        :rows="query.rows"
        empty-icon="Tools"
        empty-title="还没有工具"
        empty-desc="试试调整筛选条件,或新增一个工具"
        @page-change="onPageChange"
      ><el-table-column prop="gatewayId" label="Gateway" min-width="180">
          <template #default="{ row }">
            <IdCell :value="row.gatewayId" :max="40" prefix="" />
          </template>
        </el-table-column>

        <el-table-column prop="toolId" label="Tool ID" min-width="180">
          <template #default="{ row }">
            <IdCell :value="row.toolId" :max="40" />
          </template>
        </el-table-column>

        <el-table-column prop="toolName" label="工具名称" min-width="200">
          <template #default="{ row }">
            <span class="cell-strong" :title="row.toolName || ''">{{ row.toolName || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="toolDescription" label="描述" min-width="200">
          <template #default="{ row }">
            <span class="muted-desc" :title="row.toolDescription || '— 暂无描述 —'">{{ row.toolDescription || '— 暂无描述 —' }}</span>
          </template>
        </el-table-column>
<el-table-column prop="toolType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <StatusPill tone="info">{{ row.toolType || '—' }}</StatusPill>
          </template>
        </el-table-column>

        <el-table-column prop="toolVersion" label="版本" width="100" align="center">
          <template #default="{ row }">
            <span class="ver-pill">v{{ row.toolVersion || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="protocolId" label="协议" min-width="180">
          <template #default="{ row }">
            <IdCell v-if="row.protocolId" :value="row.protocolId" :max="40" />
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="protocolType" label="协议类型" width="120" align="center">
          <template #default="{ row }">
            <StatusPill tone="warning">{{ row.protocolType || '—' }}</StatusPill>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" fixed="right" align="right">
          <template #default="{ row }">
            <div class="ops">
              <button class="op-btn" @click="openEdit(row)">编辑</button>
              <button class="op-btn op-danger" @click="pendingDelete = row">删除</button>
            </div>
          </template>
        </el-table-column>
      </ElGrid>
    </PageCard>
<el-dialog v-model="dialog" width="680px" align-center
      :title="editMode === 'create' ? '新增网关工具' : '修改网关工具'"
      :show-close="false">
      <div class="form-stack">
        <div class="form-row-2">
          <div class="form-item">
            <label>选择网关 *</label>
            <select v-model="form.gatewayId" class="inp" :disabled="editMode==='edit'">
              <option value="">请选择…</option>
              <option v-for="g in gateways" :key="g.gatewayId" :value="g.gatewayId">
                {{ g.gatewayName }} ({{ g.gatewayId }})
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>Tool ID</label>
            <input v-model="form.toolId" type="number" class="inp" :readonly="editMode==='edit'" />
            <small class="hint">新建时自动生成 8 位数字 ID</small>
          </div>
        </div>

        <div class="form-item">
          <label>工具名称 *</label>
          <input v-model="form.toolName" type="text" class="inp" placeholder="例如 JavaSDKMCPClient_getCompanyEmployee" />
        </div>

        <div class="form-item">
          <label>工具描述</label>
          <textarea v-model="form.toolDescription" class="inp ta" rows="3" placeholder="简要描述这个工具的功能" />
        </div>

        <div class="form-row-3">
          <div class="form-item">
            <label>工具类型</label>
            <select v-model="form.toolType" class="inp">
              <option value="function">function</option>
              <option value="resource">resource</option>
            </select>
          </div>
          <div class="form-item">
            <label>协议类型</label>
            <select v-model="form.protocolType" class="inp">
              <option value="http">http</option>
              <option value="dubbo">dubbo</option>
              <option value="rabbitmq">rabbitmq</option>
            </select>
          </div>
          <div class="form-item">
            <label>版本</label>
            <input v-model="form.toolVersion" type="text" class="inp" placeholder="1.0.0" />
          </div>
        </div>

        <div class="form-item">
          <label>关联协议</label>
          <select v-model="form.protocolId" class="inp">
            <option :value="null">不绑定</option>
            <option v-for="p in protocols" :key="p.protocolId" :value="p.protocolId">
              {{ p.protocolName || p.httpUrl }} (#{{ p.protocolId }})
            </option>
          </select>
        </div>

        <div v-if="selectedProtocol" class="protocol-preview">
          <span class="eyebrow">协议预览</span>
          <div class="preview-body">
            <div class="preview-top">
              <StatusPill :tone="(selectedProtocol.httpMethod||'').toUpperCase()==='GET' ? 'info' : 'success'">
                {{ selectedProtocol.httpMethod }}
              </StatusPill>
              <code class="url">{{ selectedProtocol.httpUrl }}</code>
            </div>
            <div class="preview-sub">
              <span>超时 {{ selectedProtocol.timeout ?? '-' }} ms</span>
              <span>·</span>
              <span>映射 {{ selectedProtocol.mappings?.length || 0 }} 项</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="dialog = false">取消</button>
        <button class="btn btn-primary" :disabled="submitting" @click="onSubmit">保存</button>
      </template>
    </el-dialog>
    <ConfirmDialog
      v-if="pendingDelete"
      title="删除工具"
      :desc="`确认要删除网关 ${pendingDelete.gatewayId} 的工具 #${pendingDelete.toolId} 吗?`"
      ok-text="确认删除"
      tone="danger"
      @confirm="doDelete"
      @cancel="pendingDelete = null"
    />
  </div>
</template>
<style scoped>
.tool-page { width: 100%; }

.filter {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: 12px;
  margin-bottom: 18px;
}
.field { display: flex; flex-direction: column; gap: 6px; min-width: 200px; max-width: 280px; }
.field label { font-size: var(--fs-sm); font-weight: 500; color: var(--text-default); }
.actions { display: flex; gap: 8px; height: 40px; align-items: center; }
.toolbar { display: flex; gap: 8px; height: 40px; align-items: center; margin-left: auto; }
.hint { color: var(--text-faint); font-size: 11px; margin-top: 2px; }

:deep(.cell-strong) {
  display: block;
  font-weight: var(--fw-semibold);
  color: var(--text-strong);
  font-size: var(--fs-base);
  line-height: 1.3;
  letter-spacing: var(--ls-snug);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
}
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
:deep(.muted) { color: var(--text-faint); }

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
:deep(.icon-btn.danger:hover) {
  color: var(--err-color);
  background: var(--err-soft);
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
.op-btn.op-danger { color: var(--text-muted); }
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

.form-stack { display: flex; flex-direction: column; gap: 14px; }
.form-row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-row-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 14px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-item label { font-size: var(--fs-sm); font-weight: 500; color: var(--text-default); }

.protocol-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border-radius: var(--radius-lg);
  background: var(--info-soft);
  border: 1px solid var(--info-line);
}
.preview-top { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.preview-body { display: flex; flex-direction: column; gap: 4px; font-size: var(--fs-sm); color: var(--text-muted); }
.preview-sub { display: flex; gap: 6px; font-size: var(--fs-xs); color: var(--text-subtle); }
.preview-body code.url {
  font-family: 'JetBrains Mono', monospace;
  background: var(--bg-elevated);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--hairline);
  font-size: var(--fs-xs);
  color: var(--text-strong);
}
</style>