<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import IdCell from '@/components/ui/IdCell.vue'
import VxeGrid from '@/components/ui/VxeGrid.vue'
import {
  pageGatewayAuth,
  saveGatewayAuth,
  updateGatewayAuth,
  deleteGatewayAuth,
  copyText,
} from '@/api/admin'
import { fmtDate, localInputDate } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ gatewayId: '', page: 1, rows: 10 })

const dialog = ref(false)
const submitting = ref(false)
const editMode = ref('create')
const form = reactive(resetForm())

function resetForm() {
  const d = new Date()
  d.setMonth(d.getMonth() + 1)
  return {
    gatewayId: '',
    rateLimit: 100,
    expireTime: localInputDate(d),
  }
}

async function load() {
  loading.value = true
  try {
    const { list: data, total: t } = await pageGatewayAuth({
      gatewayId: query.gatewayId || undefined,
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
  query.page = 1
  load()
}

function onPageChange({ page, rows }) {
  query.page = page
  query.rows = rows
  load()
}

function openCreate() {
  Object.assign(form, resetForm())
  editMode.value = 'create'
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, {
    gatewayId: row.gatewayId,
    rateLimit: row.rateLimit || 100,
    expireTime: localInputDate(row.expireTime),
  })
  editMode.value = 'edit'
  dialog.value = true
}

async function onSubmit() {
  if (!form.gatewayId) return ElMessage.warning('请填写网关 ID')
  submitting.value = true
  try {
    const payload = {
      gatewayId: form.gatewayId,
      rateLimit: Number(form.rateLimit),
      expireTime: form.expireTime ? new Date(form.expireTime).getTime() : null,
    }
    if (editMode.value === 'create') {
      await saveGatewayAuth(payload)
      ElMessage.success('认证配置已添加')
    } else {
      await updateGatewayAuth(payload)
      ElMessage.success('认证配置已更新')
    }
    dialog.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function copyApiKey(row) {
  await copyText(row.apiKey)
  ElMessage.success('API Key 已复制')
}

const pendingDelete = ref(null)
async function doDelete() {
  if (!pendingDelete.value) return
  await deleteGatewayAuth(pendingDelete.value.gatewayId)
  ElMessage.success('已删除该网关的认证配置')
  pendingDelete.value = null
  load()
}
</script>

<template>
  <div class="auth-page">
    <PageCard eyebrow="API Auth" title="认证与限流" desc="为每个网关发放 API Key,设置调用速率与过期时间">
      <template #actions>
        <button class="btn" @click="load">
          <el-icon><Refresh /></el-icon> 刷新
        </button>
        <button class="btn btn--primary" @click="openCreate">
          <el-icon><Plus /></el-icon> 新增配置
        </button>
      </template>

      <div class="filter">
        <div class="field">
          <label>网关 ID</label>
          <input v-model="query.gatewayId" type="text" class="inp" placeholder="例如 gw-001" @keyup.enter="onSearch" />
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
          empty-icon="Key"
          empty-title="还没有认证配置"
          empty-desc="点击「新增配置」给某个网关发放 API Key"
          @page-change="onPageChange"
        >
          <vxe-column field="gatewayId" title="网关 ID" width="200">
            <template #default="{ row }"><IdCell :value="row.gatewayId" prefix="" /></template>
          </vxe-column>
          <vxe-column field="apiKey" title="API Key" min-width="320">
            <template #default="{ row }">
              <div class="apikey">
                <code>{{ row.apiKey }}</code>
                <button class="copy" @click="copyApiKey(row)" title="复制">
                  <el-icon><CopyDocument /></el-icon>
                </button>
              </div>
            </template>
          </vxe-column>
          <vxe-column field="rateLimit" title="限流速率" width="160">
            <template #default="{ row }">
              <StatusPill tone="info">{{ row.rateLimit }} 次/秒</StatusPill>
            </template>
          </vxe-column>
          <vxe-column field="expireTime" title="过期时间" min-width="240">
            <template #default="{ row }">{{ fmtDate(row.expireTime) }}</template>
          </vxe-column>
          <vxe-column title="操作" width="200" align="right" fixed="right">
            <template #default="{ row }">
              <div class="ops">
                <button class="btn btn--sm" @click="openEdit(row)">
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

    <el-dialog v-model="dialog" width="520px" align-center :show-close="false"
      :title="editMode === 'create' ? '新增认证配置' : '修改认证配置'">
      <div class="form-grid">
        <div class="form-item span-2">
          <label>网关 ID</label>
          <input v-model="form.gatewayId" type="text" class="inp" :readonly="editMode === 'edit'" />
        </div>
        <div class="form-item">
          <label>限流速率</label>
          <div class="suffix-wrap">
            <input v-model.number="form.rateLimit" type="number" class="inp" />
            <span class="suffix">次/秒</span>
          </div>
        </div>
        <div class="form-item">
          <label>过期时间</label>
          <input v-model="form.expireTime" type="datetime-local" class="inp" />
        </div>
      </div>
      <template #footer>
        <button class="btn" @click="dialog = false">取消</button>
        <button class="btn btn--primary" :disabled="submitting" @click="onSubmit">
          <el-icon><Check /></el-icon> 保存
        </button>
      </template>
    </el-dialog>

    <ConfirmDialog
      v-if="pendingDelete"
      title="删除认证配置"
      :desc="`确认要删除网关 ${pendingDelete.gatewayId} 的认证配置吗?`"
      ok-text="确认删除"
      tone="danger"
      @confirm="doDelete"
      @cancel="pendingDelete = null"
    />
  </div>
</template>

<style scoped lang="scss">
.auth-page { width: 100%; }

.filter {
  display: grid;
  grid-template-columns: 1fr auto;
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

.grid-host {
  border: 1px solid var(--hairline);
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
}

:deep(.apikey) {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 4px 4px 10px;
  border-radius: 7px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
}

:deep(.apikey code) {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: var(--text-default);
  max-width: 240px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.copy) {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  background: transparent;
  border: 0;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 12px;
  transition: all var(--dur-base) var(--ease);
}
:deep(.copy:hover) {
  background: var(--accent-soft);
  color: var(--accent);
}

:deep(.ops) {
  display: inline-flex;
  gap: 6px;
  justify-content: flex-end;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.form-item { display: flex; flex-direction: column; gap: 6px; position: relative; }
.form-item.span-2 { grid-column: 1 / -1; }
.form-item label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-default);
}

.suffix-wrap { position: relative; }
.suffix-wrap .suffix {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: var(--text-faint);
  pointer-events: none;
}
.suffix-wrap .inp { padding-right: 60px; }
</style>