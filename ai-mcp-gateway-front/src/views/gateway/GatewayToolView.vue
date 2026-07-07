<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import IdCell from '@/components/ui/IdCell.vue'
import VxeGrid from '@/components/ui/VxeGrid.vue'
import {
  pageGatewayTool,
  listGatewayConfig,
  listGatewayProtocol,
  saveGatewayTool,
  updateGatewayTool,
  deleteGatewayTool,
} from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const gateways = ref([])
const protocols = ref([])

const query = reactive({
  gatewayId: '',
  toolId: '',
  page: 1,
  rows: 10,
})

const dialog = ref(false)
const submitting = ref(false)
const editMode = ref('create')
const form = reactive(resetForm())

function resetForm() {
  return {
    gatewayId: '',
    toolId: null,
    toolName: '',
    toolType: 'function',
    toolDescription: '',
    toolVersion: '1.0.0',
    protocolId: null,
    protocolType: 'http',
  }
}

function genId() {
  return Math.floor(10_000_000 + Math.random() * 90_000_000)
}

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
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const settled = await Promise.allSettled([listGatewayConfig(), listGatewayProtocol()])
  gateways.value  = settled[0].status === 'fulfilled' ? settled[0].value : []
  protocols.value = settled[1].status === 'fulfilled' ? settled[1].value : []
}

onMounted(async () => {
  await load()
  await loadOptions()
})

function onSearch() {
  query.page = 1
  load()
}

function onReset() {
  query.gatewayId = ''
  query.toolId = ''
  query.page = 1
  load()
}

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
  if (!form.gatewayId) return ElMessage.warning('请选择网关')
  if (!form.toolName)  return ElMessage.warning('请填写工具名称')
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
      ElMessage.success('工具创建成功')
    } else {
      await updateGatewayTool(payload)
      ElMessage.success('工具已更新')
    }
    dialog.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const pendingDelete = ref(null)
async function doDelete() {
  if (!pendingDelete.value) return
  await deleteGatewayTool(pendingDelete.value.gatewayId, pendingDelete.value.toolId)
  ElMessage.success('工具已删除')
  pendingDelete.value = null
  load()
}

function onPageChange({ page, rows }) {
  query.page = page
  query.rows = rows
  load()
}

const selectedProtocol = computed(() =>
  protocols.value.find(p => p.protocolId === form.protocolId),
)
</script>

<template>
  <div class="tool-page">
    <PageCard eyebrow="Tool Inventory" title="网关工具管理" desc="为每个网关绑定若干 MCP 工具,每个工具可关联一条 HTTP 协议">
      <template #actions>
        <button class="btn" @click="load">
          <el-icon><Refresh /></el-icon> 刷新
        </button>
        <button class="btn btn--primary" @click="openCreate">
          <el-icon><Plus /></el-icon> 新增工具
        </button>
      </template>

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
          <button class="btn btn--ghost" @click="onReset"><el-icon><RefreshLeft /></el-icon> 重置</button>
          <button class="btn btn--primary" @click="onSearch"><el-icon><Search /></el-icon> 查询</button>
        </div>
      </div>

      <div class="grid-host">
        <VxeGrid
          :loading="loading"
          :data="list"
          :total="total"
          :page="query.page"
          :rows="query.rows"
          empty-icon="Tools"
          empty-title="还没有工具"
          empty-desc="试试调整筛选条件,或新增一个工具"
          @page-change="onPageChange"
        >
          <vxe-column field="gatewayId" title="网关" width="180">
            <template #default="{ row }"><IdCell :value="row.gatewayId" prefix="" /></template>
          </vxe-column>
          <vxe-column field="toolId" title="Tool ID" width="160">
            <template #default="{ row }"><IdCell :value="row.toolId" /></template>
          </vxe-column>
          <vxe-column field="toolName" title="工具名称" min-width="220" show-overflow="tooltip">
            <template #default="{ row }">
              <div class="cell-strong">{{ row.toolName }}</div>
            </template>
          </vxe-column>
          <vxe-column field="toolType" title="类型" width="100" align="center">
            <template #default="{ row }"><StatusPill tone="info">{{ row.toolType || '-' }}</StatusPill></template>
          </vxe-column>
          <vxe-column field="toolVersion" title="版本" width="110" align="center">
            <template #default="{ row }"><StatusPill tone="violet">v{{ row.toolVersion }}</StatusPill></template>
          </vxe-column>
          <vxe-column field="protocolId" title="协议 ID" width="140" align="center">
            <template #default="{ row }">
              <IdCell v-if="row.protocolId" :value="row.protocolId" />
              <span v-else class="muted">—</span>
            </template>
          </vxe-column>
          <vxe-column field="protocolType" title="协议类型" width="120" align="center">
            <template #default="{ row }">
              <StatusPill tone="warning">{{ row.protocolType }}</StatusPill>
            </template>
          </vxe-column>
          <vxe-column field="toolDescription" title="描述" min-width="200" show-overflow="tooltip">
            <template #default="{ row }">
              <span class="muted-desc">{{ row.toolDescription || '—' }}</span>
            </template>
          </vxe-column>
          <vxe-column title="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <div class="ops">
                <button class="btn btn--sm btn--ghost" @click="openEdit(row)">
                  <el-icon><EditPen /></el-icon> 编辑
                </button>
                <button class="btn btn--sm btn--danger" @click="pendingDelete = row">
                  <el-icon><Delete /></el-icon> 删除
                </button>
              </div>
            </template>
          </vxe-column>
        </VxeGrid>
      </div>
    </PageCard>

    <!-- form dialog -->
    <el-dialog v-model="dialog" width="640px" align-center
      :title="editMode === 'create' ? '新增网关工具' : '修改网关工具'"
      :show-close="false">
      <div class="form-grid">
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
        <div class="form-item span-2">
          <label>工具名称 *</label>
          <input v-model="form.toolName" type="text" class="inp" placeholder="例如 JavaSDKMCPClient_getCompanyEmployee" />
        </div>
        <div class="form-item">
          <label>工具类型</label>
          <select v-model="form.toolType" class="inp">
            <option value="function">function</option>
            <option value="resource">resource</option>
          </select>
        </div>
        <div class="form-item">
          <label>版本</label>
          <input v-model="form.toolVersion" type="text" class="inp" placeholder="1.0.0" />
        </div>
        <div class="form-item">
          <label>关联协议</label>
          <select v-model="form.protocolId" class="inp">
            <option :value="null">不绑定</option>
            <option v-for="p in protocols" :key="p.protocolId" :value="p.protocolId">
              #{{ p.protocolId }} · {{ p.httpUrl }}
            </option>
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
        <div class="form-item span-2">
          <label>工具描述</label>
          <textarea v-model="form.toolDescription" class="inp ta" rows="3" placeholder="简要描述这个工具的功能" />
        </div>

        <div v-if="selectedProtocol" class="protocol-preview span-2">
          <span class="eyebrow">协议预览</span>
          <div class="preview-body">
            <div class="preview-line">
              <StatusPill :tone="(selectedProtocol.httpMethod||'').toUpperCase()==='GET' ? 'info' : 'success'">
                {{ selectedProtocol.httpMethod }}
              </StatusPill>
              <code class="url">{{ selectedProtocol.httpUrl }}</code>
            </div>
            <span>{{ selectedProtocol.timeout ?? '-' }} ms · 映射 {{ selectedProtocol.mappings?.length || 0 }} 项</span>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn" @click="dialog = false">取消</button>
        <button class="btn btn--primary" :disabled="submitting" @click="onSubmit">保存</button>
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

<style scoped lang="scss">
.tool-page { width: 100%; }

.filter {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 14px;
  align-items: end;
  margin-bottom: 18px;
}

.field { display: flex; flex-direction: column; gap: 6px; }
.field label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-default);
}

.actions { display: flex; gap: 8px; height: 36px; align-items: center; }
.hint { color: var(--text-faint); font-size: 11px; margin-top: 2px; }

.grid-host {
  border: 1px solid var(--hairline);
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-item.span-2 { grid-column: 1 / -1; }
.form-item label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-default);
}

.protocol-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border-radius: 9px;
  background: var(--accent-soft);
  border: 1px solid var(--accent-line);
}

.preview-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12.5px;
  color: var(--text-muted);
  gap: 12px;
}

.preview-line { display: inline-flex; align-items: center; gap: 8px; min-width: 0; }

.preview-body code.url {
  font-family: 'JetBrains Mono', monospace;
  background: #ffffff;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid var(--hairline);
  font-size: 12px;
}

:deep(.cell-strong) {
  font-weight: 600;
  color: var(--text-strong);
}

:deep(.muted-desc) {
  color: var(--text-muted);
  font-size: 12.5px;
}

:deep(.muted) { color: var(--text-faint); }

:deep(.ops) {
  display: inline-flex;
  gap: 6px;
  justify-content: center;
}
</style>