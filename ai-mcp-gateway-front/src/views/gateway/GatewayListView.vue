<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import IdCell from '@/components/ui/IdCell.vue'
import VxeGrid from '@/components/ui/VxeGrid.vue'
import {
  pageGatewayConfig,
  saveGatewayConfig,
  updateGatewayConfig,
  buildSseUrl,
  copyText,
} from '@/api/admin'
import { authBadge, statusBadge } from '@/utils/format'

const router = useRouter()
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
  Object.assign(form, row)
  editMode.value = 'edit'
  dialog.value = true
}

async function onSubmit() {
  if (!form.gatewayId) return ElMessage.warning('请填写网关 ID')
  if (!form.gatewayName) return ElMessage.warning('请填写网关名称')
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
      ElMessage.success('网关创建成功')
    } else {
      await updateGatewayConfig(payload)
      ElMessage.success('网关已更新')
    }
    dialog.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function copySse(row) {
  try {
    await copyText(buildSseUrl(row.gatewayId))
    ElMessage.success('SSE 地址已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

function gotoDetail(row) {
  router.push(`/gateways/${row.gatewayId}`)
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
      <template #actions>
        <button class="btn" @click="load">
          <el-icon><Refresh /></el-icon> 刷新
        </button>
        <button class="btn btn--primary" @click="openCreate">
          <el-icon><Plus /></el-icon> 新增网关
        </button>
      </template>

      <!-- 过滤区 -->
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
          empty-icon="Connection"
          empty-title="暂无网关"
          empty-desc="点击右上角「新增网关」开始接入,或者尝试重置筛选条件"
          @page-change="onPageChange"
        >
          <vxe-column field="gatewayName" title="网关" min-width="260">
            <template #default="{ row }">
              <div class="gw-cell">
                <div class="gw-avatar">{{ (row.gatewayName || row.gatewayId || '?').slice(0, 1).toUpperCase() }}</div>
                <div class="gw-meta">
                  <div class="gw-name">{{ row.gatewayName || '—' }}</div>
                  <IdCell :value="row.gatewayId" />
                </div>
              </div>
            </template>
          </vxe-column>

          <vxe-column field="gatewayDesc" title="描述" min-width="220">
            <template #default="{ row }">
              <span class="muted-desc">{{ row.gatewayDesc || '—' }}</span>
            </template>
          </vxe-column>

          <vxe-column field="version" title="版本" width="110" align="center">
            <template #default="{ row }">
              <StatusPill tone="violet">v{{ row.version || '1.0.0' }}</StatusPill>
            </template>
          </vxe-column>

          <vxe-column field="auth" title="认证" width="100" align="center">
            <template #default="{ row }">
              <StatusPill :tone="authBadge(row.auth).tone">{{ authBadge(row.auth).label }}</StatusPill>
            </template>
          </vxe-column>

          <vxe-column field="status" title="状态" width="100" align="center">
            <template #default="{ row }">
              <StatusPill :tone="statusBadge(row.status).tone">{{ statusBadge(row.status).label }}</StatusPill>
            </template>
          </vxe-column>

          <vxe-column title="操作" width="260" align="right" fixed="right">
            <template #default="{ row }">
              <div class="ops">
                <el-tooltip content="复制 SSE URL" placement="top">
                  <button class="icon-btn" @click="copySse(row)">
                    <el-icon><CopyDocument /></el-icon>
                  </button>
                </el-tooltip>
                <button class="btn btn--sm btn--ghost" @click="gotoDetail(row)">
                  <el-icon><View /></el-icon> 详情
                </button>
                <button class="btn btn--sm" @click="openEdit(row)">
                  <el-icon><EditPen /></el-icon> 编辑
                </button>
              </div>
            </template>
          </vxe-column>
        </VxeGrid>
      </div>
    </PageCard>

    <!-- 表单弹窗 -->
    <el-dialog v-model="dialog" width="560px" align-center
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
          <label>认证校验 (auth)</label>
          <select v-model.number="form.auth" class="inp">
            <option :value="1">启用</option>
            <option :value="0">禁用</option>
          </select>
        </div>
        <div class="form-item span-2">
          <label>校验强度 (status)</label>
          <select v-model.number="form.status" class="inp">
            <option :value="1">强校验(1)</option>
            <option :value="0">不校验(0)</option>
          </select>
        </div>
      </div>
      <template #footer>
        <button class="btn" @click="dialog = false">取消</button>
        <button class="btn btn--primary" :disabled="submitting" @click="onSubmit">
          <el-icon v-if="!submitting"><Check /></el-icon>
          <span v-else class="loading"><span class="spinner" /></span>
          保存配置
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.gateway-list { width: 100%; }

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

.actions { display: flex; gap: 8px; align-items: center; height: 36px; }

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

.loading {
  display: inline-flex;
  align-items: center;
}
.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 720ms linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* cell helpers inside vxe-table */
:deep(.gw-cell) {
  display: flex;
  align-items: center;
  gap: 12px;
}
:deep(.gw-avatar) {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-strong);
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}
:deep(.gw-meta) { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
:deep(.gw-name) {
  font-weight: 600;
  color: var(--text-strong);
  font-size: 13.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 220px;
}

:deep(.muted-desc) {
  display: block;
  color: var(--text-muted);
  font-size: 12.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 380px;
}

:deep(.ops) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  justify-content: flex-end;
}

:deep(.icon-btn) {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  background: #ffffff;
  border: 1px solid var(--hairline);
  border-radius: 7px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--dur-base) var(--ease);
}
:deep(.icon-btn:hover) {
  background: var(--bg-deep);
  color: var(--text-strong);
  border-color: var(--hairline-strong);
}
</style>