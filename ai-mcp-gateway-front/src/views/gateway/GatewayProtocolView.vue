<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import IdCell from '@/components/ui/IdCell.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import ElGrid from '@/components/ui/ElGrid.vue'
import KeyValueCell from '@/components/ui/KeyValueCell.vue'
import HeaderChipsCell from '@/components/ui/HeaderChipsCell.vue'
import {
  pageGatewayProtocol,
  updateGatewayProtocol,
  analysisProtocol,
  importGatewayProtocol,
  deleteGatewayProtocol,
  copyText,
} from '@/api/admin'
import { httpMethodBadge } from '@/utils/format'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ protocolId: '', httpUrl: '', page: 1, rows: 10 })

async function load() {
  loading.value = true
  try {
    const { list: data, total: t } = await pageGatewayProtocol({
      protocolId: query.protocolId || undefined,
      httpUrl:    query.httpUrl    || undefined,
      page: query.page,
      rows: query.rows,
    })
    list.value = (data || []).map((r, i) => ({ ...r, _rowKey: `${r.protocolId}-${i}` }))
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

function onSearch() { query.page = 1; load() }
function onReset()  { query.protocolId = ''; query.httpUrl = ''; query.page = 1; load() }
function onPageChange({ page, rows }) { query.page = page; query.rows = rows; load() }
function methodTone(m) { return httpMethodBadge(m).tone }

async function copyUrl(row) {
  if (!row?.httpUrl) return
  try {
    await copyText(row.httpUrl)
    toast.success('已复制 URL', { duration: 1800 })
  } catch {
    toast.warning('复制失败,请手动选中', { duration: 2400 })
  }
}

const importDialog = ref(false)
const importStep   = ref('upload')
const uploadJson   = ref('')
const endpoints    = ref([])
const parsedList   = ref([])

function openImport() {
  importStep.value = 'upload'
  uploadJson.value = ''
  endpoints.value = []
  parsedList.value = []
  importDialog.value = true
}

function readFile(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    try {
      const text = String(ev.target.result || '')
      const obj = JSON.parse(text)
      if (!obj.paths || !Object.keys(obj.paths).length) {
        return toast.warning('未找到 paths 字段')
      }
      uploadJson.value = text
      endpoints.value = Object.keys(obj.paths).map((p) => ({
        path: p,
        methods: Object.keys(obj.paths[p] || {}).map((m) => m.toUpperCase()).join(', '),
        selected: true,
      }))
      importStep.value = 'pick'
    } catch (err) {
      toast.error(`JSON 解析失败: ${err.message}`, { duration: 2400 })
    }
  }
  reader.readAsText(file)
}

const pickedEndpoints = computed(() =>
  endpoints.value.filter(e => e.selected).map(e => e.path),
)
const allChecked = computed({
  get: () => endpoints.value.length > 0 && endpoints.value.every(e => e.selected),
  set: (v) => endpoints.value.forEach(e => (e.selected = !!v)),
})
const pickedCount = computed(() => pickedEndpoints.value.length)

async function runAnalysis() {
  if (!pickedCount.value) return toast.warning('请至少选择一个接口')
  importStep.value = 'parsing'
  try {
    const res = await analysisProtocol({
      openApiJson: uploadJson.value,
      endpoints:   pickedEndpoints.value,
    })
    parsedList.value = res || []
    importStep.value = 'done'
  } catch {
    importStep.value = 'pick'
  }
}

async function runImport() {
  if (!pickedCount.value) return toast.warning('请至少选择一个接口')
  if (!parsedList.value.length) return toast.warning('请先解析')
  try {
    await importGatewayProtocol({
      openApiJson: uploadJson.value,
      endpoints:   pickedEndpoints.value,
    })
    toast.success(`已导入 ${parsedList.value.length} 条协议`, { duration: 2000 })
    importDialog.value = false
    load()
  } catch (err) {
    toast.error(err?.message || '导入失败')
  }
}

const editDialog = ref(false)
const editForm = reactive({
  protocolId: null, httpUrl: '', httpMethod: 'GET', httpHeaders: '{}',
  timeout: 5000, mappingsJson: '[]', mapped: [],
})

function openEdit(row) {
  // 后端存的大小写不统一(如 "get" / "post"), select v-model 只在精确匹配时高亮,
  // 统一 toUpperCase 解决下拉框空白 + 保证保存值规范
  Object.assign(editForm, {
    protocolId: row.protocolId,
    httpUrl:    row.httpUrl,
    httpMethod: (row.httpMethod || 'GET').toUpperCase(),
    httpHeaders: row.httpHeaders || '{}',
    timeout:    row.timeout || 5000,
    mappingsJson: JSON.stringify(row.mappings || [], null, 2),
    mapped: row.mappings || [],
  })
  editDialog.value = true
}

function applyMappingsJson() {
  try {
    const arr = JSON.parse(editForm.mappingsJson || '[]')
    editForm.mapped = Array.isArray(arr) ? arr : []
    toast.success('映射已更新', { duration: 1600 })
  } catch (e) {
    toast.error(`JSON 格式不正确: ${e.message}`, { duration: 2400 })
  }
}

async function onUpdate() {
  try { JSON.parse(editForm.mappingsJson || '[]') } catch { return toast.error('映射 JSON 格式错误') }
  try {
    await updateGatewayProtocol({
      httpProtocols: [{
        protocolId:  editForm.protocolId,
        httpUrl:     editForm.httpUrl,
        // 同样归一为大写,避免再写入脏数据
        httpMethod:  (editForm.httpMethod || 'GET').toUpperCase(),
        httpHeaders: editForm.httpHeaders,
        timeout:     Number(editForm.timeout) || 5000,
        mappings:    JSON.parse(editForm.mappingsJson || '[]'),
      }],
    })
    toast.success('协议已更新', { duration: 1800 })
    editDialog.value = false
    load()
  } catch (err) {
    toast.error(err?.message || '保存失败')
  }
}

const pendingDelete = ref(null)
async function doDelete() {
  if (!pendingDelete.value) return
  try {
    await deleteGatewayProtocol(pendingDelete.value.protocolId)
    toast.success('协议已删除', { duration: 1800 })
    pendingDelete.value = null
    load()
  } catch (err) {
    toast.error(err?.message || '删除失败')
  }
}

function gotoDetail(row) { router.push(`/protocols/${row.protocolId}`) }
</script>

<template>
  <div class="protocol-page">
    <PageCard eyebrow="HTTP Protocol" title="协议配置" desc="把后端 HTTP 接口翻译为 MCP 可以理解的「工具能力」">
      <div class="filter">
        <div class="field">
          <label>协议 ID</label>
          <input v-model="query.protocolId" type="number" class="inp" placeholder="例如 1001" @keyup.enter="onSearch" />
        </div>
        <div class="field">
          <label>URL</label>
          <input v-model="query.httpUrl" type="text" class="inp" placeholder="模糊匹配,例如 /api/v1/" @keyup.enter="onSearch" />
        </div>
        <div class="actions">
          <button class="btn btn-ghost" @click="onReset"><el-icon><RefreshLeft /></el-icon> 重置</button>
          <button class="btn btn-primary" @click="onSearch"><el-icon><Search /></el-icon> 查询</button>
        </div>
        <div class="toolbar">
          <button class="btn btn-secondary" @click="load">
            <el-icon><Refresh /></el-icon> 刷新
          </button>
          <button class="btn btn-primary" @click="openImport">
            <el-icon><Upload /></el-icon> 导入 OpenAPI
          </button>
        </div>
      </div>
<ElGrid
        :data="list"
        :loading="loading"
        :total="total"
        :page="query.page"
        :rows="query.rows"
        empty-icon="Share"
        empty-title="还没有协议"
        empty-desc="试试调整筛选条件,或者导入一份 OpenAPI"
        @page-change="onPageChange"
      ><el-table-column prop="protocolId" label="ID" min-width="180">
          <template #default="{ row }">
            <IdCell :value="row.protocolId" :max="40" />
          </template>
        </el-table-column>

        <el-table-column prop="httpUrl" label="URL" min-width="220">
          <template #default="{ row }">
            <span
              class="url-cell"
              :class="{ 'is-empty': !row.httpUrl }"
              :title="row.httpUrl || '点击复制 URL'"
              @click="copyUrl(row)"
            >
              <code class="url">{{ row.httpUrl || '—' }}</code>
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="httpMethod" label="Method" width="110" align="center">
          <template #default="{ row }">
            <StatusPill :tone="methodTone(row.httpMethod)">{{ (row.httpMethod || 'UNK').toUpperCase() }}</StatusPill>
          </template>
        </el-table-column>
<el-table-column prop="timeout" label="Timeout" width="140">
          <template #default="{ row }">
            <KeyValueCell k="超时" :v="row.timeout != null ? `${row.timeout} ms` : ''" />
          </template>
        </el-table-column>

        <el-table-column prop="httpHeaders" label="Headers" min-width="220">
          <template #default="{ row }">
            <HeaderChipsCell :raw="row.httpHeaders" :max="3" />
          </template>
        </el-table-column>

        <el-table-column prop="mappings" label="映射" width="90" align="center">
          <template #default="{ row }">
            <StatusPill tone="violet">{{ row.mappings?.length || 0 }}</StatusPill>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right" align="right">
          <template #default="{ row }">
            <div class="ops">
              <button class="op-btn" @click="gotoDetail(row)">详情</button>
              <button class="op-btn" @click="openEdit(row)">编辑</button>
              <button class="op-btn op-danger" @click="pendingDelete = row">删除</button>
            </div>
          </template>
        </el-table-column>
      </ElGrid>
    </PageCard>

    <el-dialog v-model="importDialog" width="780px" align-center :show-close="false" class="import-dialog">
      <template #header>
        <div class="dlg-head">
          <span class="eyebrow">Import · 3 steps</span>
          <h2>从 OpenAPI 导入协议</h2>
          <p>上传 JSON → 选择接口 → 一键解析入库</p>
          <div class="steps">
            <div class="step" :class="{ done: importStep !== 'upload', active: importStep === 'upload' }">
              <span>1</span><small>上传</small>
            </div>
            <div class="step" :class="{ done: importStep === 'done', active: importStep === 'pick' || importStep === 'parsing' }">
              <span>2</span><small>选择接口</small>
            </div>
            <div class="step" :class="{ active: importStep === 'done' }">
              <span>3</span><small>解析结果</small>
            </div>
          </div>
        </div>
      </template>

      <div v-if="importStep === 'upload'" class="upload-zone">
        <input id="import-file" type="file" accept="application/json,.json" @change="readFile" hidden />
        <label for="import-file" class="dropzone">
          <el-icon class="drop-icon"><UploadFilled /></el-icon>
          <strong>点击上传 JSON 文件</strong>
          <small>支持 OpenAPI 3.0 / Swagger 2.0 等格式</small>
        </label>
      </div>

      <div v-else-if="importStep === 'pick'" class="pick-zone">
        <div class="pick-tools">
          <el-checkbox v-model="allChecked">全选 ({{ endpoints.length }} 个)</el-checkbox>
          <span class="select-tip">当前已选 <b>{{ pickedCount }}</b> / {{ endpoints.length }} 个接口</span>
        </div>
        <div class="endpoint-list">
          <label v-for="(e, i) in endpoints" :key="e.path" class="endpoint card card-hover" :class="{ active: e.selected }">
            <input v-model="e.selected" type="checkbox" />
            <div>
              <div class="ep-path">{{ e.path }}</div>
              <small class="ep-methods">{{ e.methods || 'ANY' }}</small>
            </div>
            <span class="ep-index">{{ String(i + 1).padStart(2, '0') }}</span>
          </label>
        </div>
      </div>

      <div v-else-if="importStep === 'parsing'" class="parsing-zone">
        <div class="spin" />
        <p>正在解析协议,请稍候…</p>
      </div>

      <div v-else-if="importStep === 'done'" class="done-zone">
        <div class="done-head">
          <el-icon><CircleCheckFilled /></el-icon>
          <strong>解析完成 · {{ parsedList.length }} 条协议</strong>
          <p>下方为解析结果预览(仅展示您在第二步选中的 {{ pickedCount }} 个接口),确认后入库。</p>
        </div>
        <div class="parsed-list">
          <div v-for="p in parsedList" :key="p.httpUrl + p.httpMethod" class="parsed-item card card-hover">
            <StatusPill :tone="httpMethodBadge(p.httpMethod).tone">{{ httpMethodBadge(p.httpMethod).label }}</StatusPill>
            <code class="url">{{ p.httpUrl }}</code>
            <span>{{ p.mappings?.length || 0 }} 项映射</span>
          </div>
        </div>
      </div>

      <template #footer>
        <button v-if="importStep === 'pick'" class="btn btn-secondary" @click="importStep = 'upload'">重新上传</button>
        <button v-if="importStep === 'pick'" class="btn btn-primary" @click="runAnalysis">
          <el-icon><DataAnalysis /></el-icon> 解析
        </button>
        <button v-if="importStep === 'done'" class="btn btn-primary" @click="runImport">
          <el-icon><Check /></el-icon> 确认导入
        </button>
        <button v-if="importStep === 'parsing'" class="btn" disabled>处理中…</button>
        <button v-if="importStep === 'upload' || importStep === 'done'" class="btn btn-secondary" @click="importDialog = false">取消</button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialog" width="780px" align-center :show-close="false">
      <template #header>
        <div class="dlg-head">
          <span class="eyebrow">Edit · Protocol</span>
          <h2>修改协议配置</h2>
          <p>编辑 URL、方法、超时与参数映射</p>
        </div>
      </template>

      <div class="form-grid">
        <div class="form-item">
          <label>协议 ID</label>
          <input v-model="editForm.protocolId" readonly class="inp" />
        </div>
        <div class="form-item">
          <label>HTTP 方法</label>
          <select v-model="editForm.httpMethod" class="inp">
            <option value="GET">GET</option>
            <option value="POST">POST</option>
            <option value="PUT">PUT</option>
            <option value="DELETE">DELETE</option>
            <option value="PATCH">PATCH</option>
          </select>
        </div>
        <div class="form-item span-2">
          <label>请求 URL</label>
          <input v-model="editForm.httpUrl" type="text" class="inp" />
        </div>
        <div class="form-item">
          <label>超时 (ms)</label>
          <input v-model.number="editForm.timeout" type="number" class="inp" />
        </div>
        <div class="form-item">
          <label>Headers (JSON)</label>
          <input v-model="editForm.httpHeaders" type="text" class="inp mono" />
        </div>
        <div class="form-item span-2">
          <label>Mappings (JSON 数组)</label>
          <textarea v-model="editForm.mappingsJson" rows="8" class="inp ta mono" />
          <button class="apply-btn" @click="applyMappingsJson">
            <el-icon><Refresh /></el-icon> 应用 JSON
          </button>
        </div>
      </div>

      <template #footer>
        <button class="btn btn-secondary" @click="editDialog = false">取消</button>
        <button class="btn btn-primary" @click="onUpdate">
          <el-icon><Check /></el-icon> 保存
        </button>
      </template>
    </el-dialog>

    <ConfirmDialog
      v-if="pendingDelete"
      title="删除协议"
      :desc="`确认要删除协议 #${pendingDelete.protocolId} 吗?`"
      ok-text="确认删除"
      tone="danger"
      @confirm="doDelete"
      @cancel="pendingDelete = null"
    />
  </div>
</template>
<style scoped>
.protocol-page { width: 100%; }

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

:deep(.url-cell) {
  cursor: pointer;
  border-radius: var(--radius-sm);
  padding: 3px 8px;
  margin: -3px -8px;
  transition: background var(--dur-fast) var(--ease-glacis);
}
:deep(.url-cell:hover) { background: var(--bg-deep); }
:deep(.url-cell:hover code.url) { color: var(--primary-600); }
:deep(code.url) {
  font-family: 'JetBrains Mono', monospace;
  font-size: var(--fs-sm);
  color: var(--text-strong);
  background: var(--bg-sunken);
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--hairline-soft);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  display: inline-block;
  letter-spacing: var(--ls-snug);
}
:deep(.url-cell.is-empty) { cursor: default; }
:deep(.url-cell.is-empty:hover) { background: transparent; }
:deep(.url-cell.is-empty code.url) {
  color: var(--text-faint);
  font-style: italic;
}

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

.dlg-head { padding: 4px 0 14px; }
.dlg-head h2 { font-size: 18px; font-weight: 700; margin-top: 8px; letter-spacing: -0.02em; color: var(--text-strong); }
.dlg-head p { margin-top: 4px; color: var(--text-muted); font-size: 12.5px; }

.steps { margin-top: 18px; display: flex; align-items: center; gap: 14px; }
.step {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: var(--radius-pill);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  font-size: 12px;
  color: var(--text-muted);
  transition: all var(--dur-base) var(--ease-glacis);
}
.step span {
  width: 18px; height: 18px; border-radius: 50%;
  background: var(--bg-elevated); color: var(--text-default);
  display: grid; place-items: center;
  font-size: 11px; font-weight: 700;
  border: 1px solid var(--hairline);
}
.step.done { border-color: var(--ok-line); background: var(--ok-soft); color: var(--ok-color); }
.step.done span { background: var(--ok-color); color: #ffffff; border-color: var(--ok-color); }
.step.active { border-color: var(--info-line); background: var(--info-soft); color: var(--primary-600); }
:root.dark .step.active { color: var(--primary-300); }
.step.active span { background: var(--primary-500); color: #ffffff; border-color: var(--primary-500); }

.upload-zone { display: flex; justify-content: center; padding: 18px 0; }
.dropzone {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  width: 100%; padding: 56px 24px;
  border-radius: var(--radius-2xl);
  border: 2px dashed var(--hairline-strong);
  background: var(--bg-sunken);
  color: var(--text-default);
  cursor: pointer;
  transition: all var(--dur-base) var(--ease-glacis);
}
.dropzone:hover {
  background: var(--info-soft);
  border-color: var(--primary-500);
  color: var(--primary-600);
  transform: scale(1.005);
}
:root.dark .dropzone:hover { color: var(--primary-300); }
.drop-icon { font-size: 36px; color: var(--primary-500); }
.dropzone strong { font-size: 15px; color: var(--text-strong); }
.dropzone small  { font-size: 12px; color: var(--text-muted); }

.pick-tools { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.select-tip { font-size: 12px; color: var(--text-muted); }

.endpoint-list {
  display: flex; flex-direction: column; gap: 6px;
  max-height: 360px; overflow-y: auto;
  padding: 4px;
}
.endpoint {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: var(--radius-lg);
  background: var(--bg-elevated);
  border: 1px solid var(--hairline);
  cursor: pointer;
  transition: all var(--dur-base) var(--ease-glacis);
}
.endpoint:hover { border-color: var(--info-line); transform: translateY(-1px); }
.endpoint.active { background: var(--info-soft); border-color: var(--info-line); }
.ep-path { font-family: 'JetBrains Mono', monospace; font-size: 13px; font-weight: 600; color: var(--text-strong); }
.ep-methods { font-family: 'JetBrains Mono', monospace; font-size: 11.5px; color: var(--text-muted); }
.ep-index { font-family: 'JetBrains Mono', monospace; font-size: 11px; color: var(--text-faint); }

.parsing-zone { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 64px 0; }
.spin {
  width: 44px; height: 44px;
  border-radius: 50%;
  border: 3px solid var(--info-soft);
  border-top-color: var(--primary-500);
  animation: spin 720ms linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.done-zone { display: flex; flex-direction: column; gap: 14px; }
.done-head { display: grid; grid-template-columns: auto 1fr; align-items: center; column-gap: 12px; row-gap: 2px; }
.done-head .el-icon { font-size: 22px; color: var(--ok-color); }
.done-head strong { font-size: 15px; color: var(--text-strong); }
.done-head p { grid-column: 2 / 3; font-size: 12.5px; color: var(--text-muted); }

.parsed-list { display: flex; flex-direction: column; gap: 8px; max-height: 320px; overflow-y: auto; }
.parsed-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px;
  border-radius: var(--radius-lg);
  background: var(--bg-elevated);
  border: 1px solid var(--hairline);
  font-size: 12.5px;
  color: var(--text-muted);
}
.parsed-item code.url { flex: 1; color: var(--text-strong); }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-item { display: flex; flex-direction: column; gap: 6px; position: relative; }
.form-item.span-2 { grid-column: 1 / -1; }
.form-item label { font-size: 12px; font-weight: 500; color: var(--text-default); }
.inp.mono { font-family: 'JetBrains Mono', monospace; font-size: 12.5px; }

.apply-btn {
  align-self: flex-end;
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: var(--radius-md);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-default);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--dur-base) var(--ease-glacis);
  font-family: inherit;
}
.apply-btn:hover {
  background: var(--info-soft);
  color: var(--primary-600);
  border-color: var(--info-line);
}
:root.dark .apply-btn:hover { color: var(--primary-300); }
</style>