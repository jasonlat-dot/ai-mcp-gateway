<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import IdCell from '@/components/ui/IdCell.vue'
import HeaderChips from '@/components/ui/HeaderChips.vue'
import ElGrid from '@/components/ui/ElGrid.vue'
import {
  listGatewayProtocol,
  pageGatewayProtocol,
  copyText,
} from '@/api/admin'
import { httpMethodBadge } from '@/utils/format'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const id = computed(() => route.params.id)
const protocol = ref(null)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    let target = null
    try {
      // 后端 page 接口响应: { code, info, data: [...], total: 'N' }
      // unpack 已统一返回 { list, total }
      const page = await pageGatewayProtocol({ protocolId: String(id.value), page: 1, rows: 100 })
      const rows = Array.isArray(page) ? page : (page?.list || [])
      target = (rows || []).find(p => String(p.protocolId) === String(id.value)) || null
    } catch {}
    if (!target) {
      const list = await listGatewayProtocol()
      target = (list || []).find(p => String(p.protocolId) === String(id.value))
    }
    protocol.value = target || null
    if (target && Array.isArray(target.mappings)) {
      target.mappings = target.mappings.map((m, i) => ({ ...m, _rowKey: `${m.fieldName || 'mapping'}-${i}` }))
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)

function back() { router.push('/protocols') }

async function copyUrl(u) {
  if (!u) return
  try {
    await copyText(u)
    toast.success('已复制', { duration: 1600 })
  } catch {
    toast.error('复制失败', { duration: 1600 })
  }
}
</script>

<template>
  <div class="pd">
    <header class="hero">
      <div class="hero-left">
        <span class="eyebrow">Protocol · #{{ id }}</span>
        <h1 v-if="protocol">
          <StatusPill :tone="httpMethodBadge(protocol.httpMethod).tone">{{ httpMethodBadge(protocol.httpMethod).label }}</StatusPill>
          <code class="url">{{ protocol.httpUrl }}</code>
        </h1>
        <h1 v-else>未找到协议 #{{ id }}</h1>
      </div>
      <div class="hero-right">
        <button class="btn btn-secondary" @click="copyUrl(protocol?.httpUrl)">
          <el-icon><CopyDocument /></el-icon> 复制 URL
        </button>
        <button class="btn btn-primary" @click="back">
          <el-icon><Back /></el-icon> 返回
        </button>
      </div>
    </header>

    <div v-if="loading" class="loading-state">
      <div class="spinner" /> 加载中…
    </div>

    <template v-else-if="protocol">
      <PageCard eyebrow="HTTP" title="请求信息" desc="发起该协议时的基础字段">
        <div class="kv-grid kv-grid-3">
          <div class="kv card-hover"><span>请求方法</span><StatusPill :tone="httpMethodBadge(protocol.httpMethod).tone">{{ protocol.httpMethod }}</StatusPill></div>
          <div class="kv card-hover"><span>超时</span><b>{{ protocol.timeout ?? '-' }} ms</b></div>
          <div class="kv span-2 card-hover kv-headers">
            <span>Headers</span>
            <HeaderChips :raw="protocol.httpHeaders" />
          </div>
          <div class="kv span-3 card-hover"><span>URL</span><code class="url">{{ protocol.httpUrl }}</code></div>
        </div>
      </PageCard>

      <PageCard eyebrow="Mappings" title="参数映射" :desc="`共 ${protocol.mappings?.length || 0} 项字段映射`">
        <ElGrid
          :data="protocol.mappings || []"
          :show-pager="false"
          empty-icon="Connection"
          empty-title="没有参数映射"
          empty-desc="该协议尚未配置字段映射"
        >
          <el-table-column prop="mappingType" label="类型" width="100" align="center">
            <template #default="{ row }">
              <StatusPill :tone="row.mappingType === 'request' ? 'info' : 'success'">{{ row.mappingType }}</StatusPill>
            </template>
          </el-table-column>
          <el-table-column prop="parentPath" label="父级" width="180">
            <template #default="{ row }">
              <code v-if="row.parentPath" class="mono" :title="row.parentPath">{{ row.parentPath }}</code>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="fieldName" label="字段名" min-width="180">
            <template #default="{ row }"><b :title="row.fieldName || ''">{{ row.fieldName }}</b></template>
          </el-table-column>
          <el-table-column prop="mcpPath" label="MCP Path" min-width="220">
            <template #default="{ row }"><code class="mono" :title="row.mcpPath">{{ row.mcpPath }}</code></template>
          </el-table-column>
          <el-table-column prop="mcpType" label="MCP 类型" width="120" align="center">
            <template #default="{ row }"><StatusPill tone="violet">{{ row.mcpType }}</StatusPill></template>
          </el-table-column>
          <el-table-column prop="isRequired" label="必填" width="90" align="center">
            <template #default="{ row }">
              <StatusPill :tone="row.isRequired === 1 ? 'danger' : 'default'">
                {{ row.isRequired === 1 ? '是' : '否' }}
              </StatusPill>
            </template>
          </el-table-column>
          <el-table-column prop="mcpDesc" label="描述" min-width="200">
            <template #default="{ row }"><span class="muted" :title="row.mcpDesc || '—'">{{ row.mcpDesc || '—' }}</span></template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80" align="center">
            <template #default="{ row }">{{ row.sortOrder ?? '-' }}</template>
          </el-table-column>
        </ElGrid>
      </PageCard>
    </template>

    <PageCard v-else eyebrow="Not Found" title="协议不存在" desc="它可能已被删除,或 ID 输入有误。" />
  </div>
</template>

<style scoped>
.pd { display: flex; flex-direction: column; gap: 22px; animation: fadeIn 0.3s ease-out; }

.hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
  padding: 4px;
}
.hero-left h1 {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-top: 8px;
  flex-wrap: wrap;
  color: var(--text-strong);
}
.hero-left .url {
  font-family: 'JetBrains Mono', monospace;
  font-size: 16px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  padding: 6px 12px;
  border-radius: var(--radius-md);
  color: var(--text-strong);
}
.hero-right { display: flex; gap: 10px; flex-shrink: 0; }

.kv-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.kv-grid-3 { grid-template-columns: repeat(3, 1fr); }
@media (max-width: 1080px) { .kv-grid-3 { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 720px)  { .kv-grid-3 { grid-template-columns: 1fr; } }
.kv {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px;
  border-radius: var(--radius-lg);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
  transition: all var(--dur-base) var(--ease-glacis);
}
.kv:hover {
  background: var(--bg-elevated);
  border-color: var(--info-line);
  transform: translateY(-1px);
}
.kv.span-2 { grid-column: span 2; }
.kv.span-3 { grid-column: 1 / -1; }
.kv-headers { display: flex; flex-direction: column; gap: 8px; }
.kv > span {
  font-size: 11px;
  letter-spacing: 0.02em;
  color: var(--text-faint);
  font-weight: 500;
}
.kv > code,
.kv > b {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  color: var(--text-strong);
  font-weight: 500;
}

/* ===== table cell 字体 ===== */
:deep(.muted) { color: var(--text-muted); font-size: 12.5px; }
:deep(.mono)  {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.loading-state {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  padding: 80px 16px;
  color: var(--text-muted);
}
.spinner {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2.5px solid var(--info-soft);
  border-top-color: var(--primary-500);
  animation: spin 720ms linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 720px) {
  .kv-grid { grid-template-columns: 1fr; }
  .hero { flex-direction: column; align-items: flex-start; }
}
</style>
