<script setup>
import { ref, reactive, onMounted } from 'vue'
import PageCard from '@/components/ui/PageCard.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import IdCell from '@/components/ui/IdCell.vue'
import ElGrid from '@/components/ui/ElGrid.vue'
import KeyValueCell from '@/components/ui/KeyValueCell.vue'
import {
  pageGatewayAuth,
  saveGatewayAuth,
  updateGatewayAuth,
  deleteGatewayAuth,
  listGatewayConfig,
} from '@/api/admin'
import { fmtDate, localInputDate, inputDateOrEmpty } from '@/utils/format'
import { useToast } from '@/composables/useToast'

const toast = useToast()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ gatewayId: '', page: 1, rows: 10 })

/* 已配置的网关列表,用于新增认证时的下拉选择 */
const gateways = ref([])
const gatewaysLoading = ref(false)

const dialog = ref(false)
const submitting = ref(false)
const editMode = ref('create')
const form = reactive(resetForm())

/**
 * 过期时间快捷选项
 *  - value: 'never' | 数字(天数)
 *  - 永不过期: form.expireTime = '' (后端用 null 存)
 *  - 其它: form.expireTime = 本地 datetime-local 字符串
 */
const EXPIRE_SHORTCUTS = [
  { key: '1d',  label: '1 天',  days: 1   },
  { key: '3d',  label: '3 天',  days: 3   },
  { key: '1w',  label: '1 周',  days: 7   },
  { key: '1m',  label: '1 个月', days: 30  },
  { key: '1y',  label: '1 年',  days: 365 },
  { key: 'never', label: '永不过期', days: null },
]

function resetForm() {
  const d = new Date()
  d.setMonth(d.getMonth() + 1)
  return { gatewayId: '', rateLimit: 100, expireTime: localInputDate(d) }
}

/** 把指定天数(从今天起)写入 form.expireTime; days=null 表示永不过期 */
function applyExpireShortcut(days) {
  if (days == null) {
    form.expireTime = ''   // 空字符串 → onSubmit 时会转 null → 后端永不过期
    return
  }
  const d = new Date()
  d.setDate(d.getDate() + days)
  form.expireTime = localInputDate(d)
}

/** 判断当前 form.expireTime 是否对应该快捷(用于高亮) */
function isExpireShortcutActive(days) {
  if (days == null) {
    return !form.expireTime
  }
  if (!form.expireTime) return false
  // 解析为本地时区时间
  const cur = new Date(form.expireTime)
  if (isNaN(cur.getTime())) return false
  const target = new Date()
  target.setDate(target.getDate() + days)
  // 容差 ±60 秒(datetime-local 截断到分钟)
  return Math.abs(cur.getTime() - target.getTime()) < 60_000
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
  } catch (err) {
    list.value = []
    total.value = 0
    toast.error(err?.message || '加载列表失败')
  } finally {
    loading.value = false
  }
}

async function loadGateways() {
  gatewaysLoading.value = true
  try {
    const data = await listGatewayConfig()
    gateways.value = (data || []).map((g) => ({
      gatewayId:   g.gatewayId,
      gatewayName: g.gatewayName,
    }))
  } catch {
    gateways.value = []
  } finally {
    gatewaysLoading.value = false
  }
}

onMounted(() => {
  load()
  loadGateways()
})

function onSearch() { query.page = 1; load() }
function onReset()  { query.gatewayId = ''; query.page = 1; load() }
function onPageChange({ page, rows }) { query.page = page; query.rows = rows; load() }

function openCreate() {
  Object.assign(form, resetForm())
  editMode.value = 'create'
  dialog.value = true
  // 兜底:如果列表为空,异步拉一次
  if (gateways.value.length === 0) loadGateways()
}

function openEdit(row) {
  Object.assign(form, {
    gatewayId: row.gatewayId,
    rateLimit: row.rateLimit || 100,
    // 后端 null 表示「永不过期」,空字符串也与 null 等价,以便快捷按钮「永不过期」可点亮
    expireTime: row.expireTime == null ? '' : inputDateOrEmpty(row.expireTime),
  })
  editMode.value = 'edit'
  dialog.value = true
}

async function onSubmit() {
  if (!form.gatewayId) return toast.warning('请填写网关 ID')
  submitting.value = true
  try {
    const payload = {
      gatewayId: form.gatewayId,
      rateLimit: Number(form.rateLimit),
      // 空字符串 / undefined → null(永不过期);否则转时间戳
      expireTime: form.expireTime ? new Date(form.expireTime).getTime() : null,
    }
    if (editMode.value === 'create') {
      await saveGatewayAuth(payload)
      toast.success('认证配置已添加', { duration: 1800 })
    } else {
      await updateGatewayAuth(payload)
      toast.success('认证配置已更新', { duration: 1800 })
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
    await deleteGatewayAuth(pendingDelete.value.gatewayId)
    toast.success('已删除该网关的认证配置', { duration: 1800 })
    pendingDelete.value = null
    load()
  } catch (err) {
    toast.error(err?.message || '删除失败')
  }
}
</script>

<template>
  <div class="auth-page">
    <PageCard eyebrow="API Auth" title="认证与限流" desc="为每个网关发放 API Key,设置调用速率与过期时间">
      <div class="filter">
        <div class="field">
          <label>网关 ID</label>
          <input v-model="query.gatewayId" type="text" class="inp" placeholder="例如 gw-001" @keyup.enter="onSearch" />
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
            <el-icon><Plus /></el-icon> 新增配置
          </button>
        </div>
      </div>

      <ElGrid
        :data="list"
        :loading="loading"
        :total="total"
        :page="query.page"
        :rows="query.rows"
        empty-icon="Key"
        empty-title="还没有认证配置"
        empty-desc="点击「新增配置」给某个网关发放 API Key"
        @page-change="onPageChange"
      ><el-table-column prop="gatewayId" label="Gateway ID" min-width="180">
          <template #default="{ row }">
            <IdCell :value="row.gatewayId" :max="40" prefix="" />
          </template>
        </el-table-column>

        <el-table-column prop="apiKey" label="API Key" min-width="220">
          <template #default="{ row }">
            <IdCell :value="row.apiKey" prefix="" :truncate="false" tone="warn" toast-msg="API Key 已复制" />
          </template>
        </el-table-column>

        <el-table-column prop="rateLimit" label="限流" width="190">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.rateLimit != null"
              placement="top"
              :show-after="120"
              :content="`每小时允许 ${row.rateLimit} 次;后端会按 3600 平摊到秒,即约 ${(row.rateLimit / 3600).toFixed(4)} 次/秒`"
            >
              <span>
                <KeyValueCell
                  k="限流"
                  :v="`${row.rateLimit} 次/小时`"
                  copyable
                  tone="info"
                />
              </span>
            </el-tooltip>
            <KeyValueCell v-else k="限流" :v="'永不限流'" copyable tone="info" />
          </template>
        </el-table-column>

        <el-table-column prop="expireTime" label="到期" width="200">
          <template #default="{ row }">
            <KeyValueCell k="到期" :v="row.expireTime == null ? '永不过期' : fmtDate(row.expireTime)" />
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

    <el-dialog v-model="dialog" width="560px" align-center :show-close="false"
      :title="editMode === 'create' ? '新增认证配置' : '修改认证配置'">
      <div class="form-grid">
        <div class="form-item span-2">
          <label>
            网关
            <span v-if="editMode === 'create'" class="hint">从已有网关中选择</span>
            <span v-else class="hint">已绑定的网关,不允许修改</span>
          </label>
          <!-- 新增:从已有网关列表下拉选择(支持搜索 + 清空) -->
          <el-select
            v-if="editMode === 'create'"
            v-model="form.gatewayId"
            class="inp gateway-select"
            placeholder="搜索 / 选择一个已配置的网关"
            filterable
            clearable
            :loading="gatewaysLoading"
            no-data-text="还没有已配置的网关,请先到「网关列表」新增一个"
          >
            <el-option
              v-for="g in gateways"
              :key="g.gatewayId"
              :value="g.gatewayId"
              :label="g.gatewayName ? `${g.gatewayName} (${g.gatewayId})` : g.gatewayId"
            />
          </el-select>
          <!-- 编辑:展示只读,避免改错 -->
          <input
            v-else
            v-model="form.gatewayId"
            type="text"
            class="inp"
            readonly
          />
        </div>
        <div class="form-item span-2">
          <label>
            限流速率
            <span class="hint">每小时允许调用的最大次数(后端会按 3600 平摊为秒级速率,即 N/3600 次/秒)</span>
          </label>
          <div class="suffix-wrap">
            <input
              v-model.number="form.rateLimit"
              type="number"
              class="inp"
              min="0"
              placeholder="例如 100 = 每小时 100 次 ≈ 0.0278 次/秒"
            />
            <span class="suffix">次 / 小时</span>
          </div>
        </div>
        <div class="form-item span-2">
          <label>过期时间 <span class="hint">空值表示「永不过期」,后端存 null</span></label>
          <input
            v-model="form.expireTime"
            type="datetime-local"
            class="inp"
            placeholder="留空表示永不过期"
          />
          <div class="expire-shortcuts">
            <span class="expire-shortcuts-label">快捷设置:</span>
            <button
              v-for="opt in EXPIRE_SHORTCUTS"
              :key="opt.key"
              type="button"
              class="expire-chip"
              :class="{ active: isExpireShortcutActive(opt.days) }"
              @click="applyExpireShortcut(opt.days)"
            >{{ opt.label }}</button>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="dialog = false">取消</button>
        <button class="btn btn-primary" :disabled="submitting" @click="onSubmit">
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

<style scoped>
.auth-page { width: 100%; }

.filter {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: 12px;
  margin-bottom: 18px;
}
.field { display: flex; flex-direction: column; gap: 6px; min-width: 200px; max-width: 280px; }
.field label { font-size: 12px; font-weight: 500; color: var(--text-default); }
.actions { display: flex; gap: 8px; height: 40px; align-items: center; }
.toolbar { display: flex; gap: 8px; height: 40px; align-items: center; margin-left: auto; }

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

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-item { display: flex; flex-direction: column; gap: 6px; position: relative; }
.form-item.span-2 { grid-column: 1 / -1; }
.form-item label { font-size: 12px; font-weight: 500; color: var(--text-default); }

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
.suffix-wrap .inp { padding-right: 78px; }

.form-item .hint {
  font-weight: 400;
  color: var(--text-faint);
  font-size: 11px;
  margin-left: 6px;
}

/* el-select 在 form-item 中需要占满整行,且与 .inp 同高 */
.gateway-select {
  display: block;
  width: 100%;
}
.gateway-select :deep(.el-select__wrapper) {
  background: var(--bg-sunken);
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--hairline) inset;
  padding: 4px 12px;
  min-height: 38px;
}
.gateway-select :deep(.el-select__wrapper.is-hovering) {
  box-shadow: 0 0 0 1px var(--info-line) inset;
}
.gateway-select :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1.5px var(--primary-500) inset;
}
:root.dark .gateway-select :deep(.el-select__wrapper) {
  background: var(--bg-deep);
  box-shadow: 0 0 0 1px var(--hairline-soft) inset;
}
:root.dark .gateway-select :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1.5px var(--primary-300) inset;
}

.expire-shortcuts {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}
.expire-shortcuts-label {
  font-size: 11px;
  color: var(--text-faint);
  margin-right: 4px;
}
.expire-chip {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid var(--hairline);
  background: var(--bg-deep);
  color: var(--text-muted);
  font-family: inherit;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  transition: all .15s ease;
  white-space: nowrap;
}
.expire-chip:hover {
  color: var(--primary-600);
  border-color: var(--primary-300);
  background: var(--primary-100);
}
.expire-chip.active {
  color: var(--primary-600);
  border-color: var(--primary-300);
  background: var(--primary-100);
  font-weight: 600;
  box-shadow: 0 0 0 1.5px var(--primary-300) inset;
}
:root.dark .expire-chip {
  border-color: var(--hairline-soft);
  color: var(--text-muted);
}
:root.dark .expire-chip:hover,
:root.dark .expire-chip.active {
  color: var(--primary-300);
  border-color: rgba(20, 184, 166, 0.45);
  background: rgba(20, 184, 166, 0.10);
  box-shadow: 0 0 0 1.5px rgba(20, 184, 166, 0.45) inset;
}
</style>