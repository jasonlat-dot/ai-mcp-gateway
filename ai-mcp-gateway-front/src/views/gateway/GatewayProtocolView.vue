<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import IdCell from '@/components/ui/IdCell.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import VxeGrid from '@/components/ui/VxeGrid.vue'
import {
  pageGatewayProtocol,
  updateGatewayProtocol,
  analysisProtocol,
  importGatewayProtocol,
  deleteGatewayProtocol,
} from '@/api/admin'
import { httpMethodBadge } from '@/utils/format'

const router = useRouter()
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
  query.protocolId = ''
  query.httpUrl = ''
  query.page = 1
  load()
}

function onPageChange({ page, rows }) {
  query.page = page
  query.rows = rows
  load()
}

/* ============ 导入对话框 ============ */
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
        return ElMessage.warning('未找到 paths 字段')
      }
      uploadJson.value = text
      endpoints.value = Object.keys(obj.paths).map((p) => ({
        path: p,
        methods: Object.keys(obj.paths[p] || {}).map((m) => m.toUpperCase()).join(', '),
        selected: true,
      }))
      importStep.value = 'pick'
    } catch (err) {
      ElMessage.error(`JSON 解析失败: ${err.message}`)
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
  if (!pickedCount.value) {
    return ElMessage.warning('请至少选择一个接口')
  }
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
  if (!pickedCount.value) {
    return ElMessage.warning('请至少选择一个接口')
  }
  if (!parsedList.value.length) {
    return ElMessage.warning('请先解析')
  }
  await importGatewayProtocol({
    openApiJson: uploadJson.value,
    endpoints:   pickedEndpoints.value,
  })
  ElMessage.success(`已导入 ${parsedList.value.length} 条协议`)
  importDialog.value = false
  load()
}

/* ============ 修改 (重写映射) 对话框 ============ */
const editDialog = ref(false)
const editForm = reactive({
  protocolId: null,
  httpUrl: '',
  httpMethod: 'GET',
  httpHeaders: '{}',
  timeout: 5000,
  mappingsJson: '[]',
  mapped: [],
})

function openEdit(row) {
  Object.assign(editForm, {
    protocolId: row.protocolId,
    httpUrl:    row.httpUrl,
    httpMethod: row.httpMethod || 'GET',
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
    ElMessage.success('映射已更新')
  } catch (e) {
    ElMessage.error(`JSON 格式不正确: ${e.message}`)
  }
}

async function onUpdate() {
  try {
    JSON.parse(editForm.mappingsJson || '[]')
  } catch {
    return ElMessage.error('映射 JSON 格式错误')
  }
  await updateGatewayProtocol({
    httpProtocols: [
      {
        protocolId:  editForm.protocolId,
        httpUrl:     editForm.httpUrl,
        httpMethod:  editForm.httpMethod,
        httpHeaders: editForm.httpHeaders,
        timeout:     Number(editForm.timeout) || 5000,
        mappings:    JSON.parse(editForm.mappingsJson || '[]'),
      },
    ],
  })
  ElMessage.success('协议已更新')
  editDialog.value = false
  load()
}

const pendingDelete = ref(null)
async function doDelete() {
  if (!pendingDelete.value) return
  await deleteGatewayProtocol(pendingDelete.value.protocolId)
  ElMessage.success('协议已删除')
  pendingDelete.value = null
  load()
}

function gotoDetail(row) {
  router.push(`/protocols/${row.protocolId}`)
}
</script>

<template>
  <div class="protocol-page">
    <PageCard eyebrow="HTTP Protocol" title="协议配置" desc="把后端 HTTP 接口翻译为 MCP 可以理解的「工具能力」">
      <template #actions>
        <button class="btn" @click="load">
          <el-icon><Refresh /></el-icon> 刷新
        </button>
        <button class="btn btn--primary" @click="openImport">
          <el-icon><Upload /></el-icon> 导入 OpenAPI
        </button>
      </template>

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
          empty-icon="Share"
          empty-title="还没有协议"
          empty-desc="试试调整筛选条件,或者导入一份 OpenAPI"
          @page-change="onPageChange"
        >
          <vxe-column field="protocolId" title="ID" width="120">
            <template #default="{ row }"><IdCell :value="row.protocolId" /></template>
          </vxe-column>
          <vxe-column field="httpUrl" title="URL" min-width="280" show-overflow="tooltip">
            <template #default="{ row }"><code class="url">{{ row.httpUrl }}</code></template>
          </vxe-column>
          <vxe-column field="httpMethod" title="方法" width="110">
            <template #default="{ row }">
              <StatusPill :tone="httpMethodBadge(row.httpMethod).tone">{{ httpMethodBadge(row.httpMethod).label }}</StatusPill>
            </template>
          </vxe-column>
          <vxe-column field="timeout" title="超时" width="100">
            <template #default="{ row }">{{ row.timeout ?? '-' }} ms</template>
          </vxe-column>
          <vxe-column field="httpHeaders" title="Headers" min-width="160" show-overflow="tooltip">
            <template #default="{ row }">
              <code class="headers">{{ row.httpHeaders || '{}' }}</code>
            </template>
          </vxe-column>
          <vxe-column field="mappings" title="映射" width="100">
            <template #default="{ row }">
              <StatusPill tone="violet">{{ row.mappings?.length || 0 }}</StatusPill>
            </template>
          </vxe-column>
          <vxe-column title="操作" width="240" align="right" fixed="right">
            <template #default="{ row }">
              <div class="ops">
                <button class="btn btn--sm btn--ghost" @click="gotoDetail(row)">
                  <el-icon><View /></el-icon> 详情
                </button>
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

    <!-- 导入对话框 -->
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
          <el-checkbox v-model="allChecked">
            全选 ({{ endpoints.length }} 个)
          </el-checkbox>
          <span class="select-tip">
            当前已选 <b>{{ pickedCount }}</b> / {{ endpoints.length }} 个接口
          </span>
        </div>
        <div class="endpoint-list">
          <label
            v-for="(e, i) in endpoints"
            :key="e.path"
            class="endpoint"
            :class="{ active: e.selected }"
          >
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
          <div v-for="p in parsedList" :key="p.httpUrl + p.httpMethod" class="parsed-item">
            <StatusPill :tone="httpMethodBadge(p.httpMethod).tone">{{ httpMethodBadge(p.httpMethod).label }}</StatusPill>
            <code class="url">{{ p.httpUrl }}</code>
            <span>{{ p.mappings?.length || 0 }} 项映射</span>
          </div>
        </div>
      </div>

      <template #footer>
        <button v-if="importStep === 'pick'" class="btn" @click="importStep = 'upload'">重新上传</button>
        <button v-if="importStep === 'pick'" class="btn btn--primary" @click="runAnalysis">
          <el-icon><DataAnalysis /></el-icon> 解析
        </button>
        <button v-if="importStep === 'done'" class="btn btn--primary" @click="runImport">
          <el-icon><Check /></el-icon> 确认导入
        </button>
        <button v-if="importStep === 'parsing'" class="btn" disabled>处理中…</button>
        <button
          v-if="importStep === 'upload' || importStep === 'done'"
          class="btn"
          @click="importDialog = false"
        >取消</button>
      </template>
    </el-dialog>

    <!-- 修改映射 -->
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
        <button class="btn" @click="editDialog = false">取消</button>
        <button class="btn btn--primary" @click="onUpdate">
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

<style scoped lang="scss">
.protocol-page { width: 100%; }

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

.grid-host {
  border: 1px solid var(--hairline);
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
}

:deep(.url), :deep(code.url) {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
}

:deep(.headers) {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11.5px;
  color: var(--text-faint);
  background: var(--bg-sunken);
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid var(--hairline-soft);
  display: inline-block;
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}

:deep(.ops) {
  display: inline-flex;
  gap: 6px;
  justify-content: flex-end;
}

/* ========== Import dialog ========== */
.dlg-head { padding: 4px 0 14px; }
.dlg-head h2 { font-size: 18px; font-weight: 700; margin-top: 8px; letter-spacing: -0.02em; }
.dlg-head p { margin-top: 4px; color: var(--text-muted); font-size: 12.5px; }

.steps {
  margin-top: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.step {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  font-size: 12px;
  color: var(--text-muted);
}

.step span {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #ffffff;
  color: var(--text-default);
  display: grid;
  place-items: center;
  font-size: 11px;
  font-weight: 700;
  border: 1px solid var(--hairline);
}

.step small { font-weight: 500; letter-spacing: 0.01em; }

.step.done {
  border-color: rgba(13, 148, 136, 0.30);
  background: var(--teal-soft);
  color: var(--teal);
}
.step.done span {
  background: var(--teal);
  color: #ffffff;
  border-color: var(--teal);
}

.step.active {
  border-color: var(--accent-line);
  background: var(--accent-soft);
  color: var(--accent);
}
.step.active span {
  background: var(--accent);
  color: #ffffff;
  border-color: var(--accent);
}

.upload-zone {
  display: flex;
  justify-content: center;
  padding: 18px 0;
}

.dropzone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 56px 24px;
  border-radius: 14px;
  border: 2px dashed var(--hairline-strong);
  background: var(--bg-sunken);
  color: var(--text-default);
  cursor: pointer;
  transition: all var(--dur-base) var(--ease);
}

.dropzone:hover {
  background: var(--accent-soft);
  border-color: var(--accent);
  color: var(--accent);
}

.drop-icon {
  font-size: 36px;
  color: var(--accent);
}

.dropzone strong { font-size: 15px; color: var(--text-strong); }
.dropzone small  { font-size: 12px; color: var(--text-muted); }

.pick-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.select-tip {
  font-size: 12px;
  color: var(--text-muted);
}

.endpoint-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 360px;
  overflow-y: auto;
  padding: 4px;
}

.endpoint {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 9px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
  cursor: pointer;
  transition: all var(--dur-base) var(--ease);
}

.endpoint:hover {
  background: var(--accent-soft);
  border-color: var(--accent-line);
}

.endpoint.active {
  background: var(--accent-soft);
  border-color: var(--accent-line);
}

.ep-path {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-strong);
}

.ep-methods {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11.5px;
  color: var(--text-muted);
}

.ep-index {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  color: var(--text-faint);
}

.parsing-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 64px 0;
}

.spin {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 3px solid var(--accent-soft);
  border-top-color: var(--accent);
  animation: spin 720ms linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.done-zone { display: flex; flex-direction: column; gap: 14px; }
.done-head { display: grid; grid-template-columns: auto 1fr; align-items: center; column-gap: 12px; row-gap: 2px; }
.done-head .el-icon { font-size: 22px; color: var(--teal); }
.done-head strong { font-size: 15px; color: var(--text-strong); }
.done-head p { grid-column: 2 / 3; font-size: 12.5px; color: var(--text-muted); }

.parsed-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 320px;
  overflow-y: auto;
}

.parsed-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 9px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
  font-size: 12.5px;
  color: var(--text-muted);
}
.parsed-item code.url { flex: 1; }

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

.inp.mono { font-family: 'JetBrains Mono', monospace; font-size: 12.5px; }

.apply-btn {
  align-self: flex-end;
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 7px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-default);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--dur-base) var(--ease);
}
.apply-btn:hover {
  background: var(--accent-soft);
  color: var(--accent);
  border-color: var(--accent-line);
}
</style>