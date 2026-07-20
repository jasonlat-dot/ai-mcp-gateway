<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import IdCell from '@/components/ui/IdCell.vue'
import ElGrid from '@/components/ui/ElGrid.vue'
import KeyValueCell from '@/components/ui/KeyValueCell.vue'
import HeaderChipsCell from '@/components/ui/HeaderChipsCell.vue'
import {
  listGatewayConfig,
  listGatewayToolByGatewayId,
  listGatewayProtocolByGatewayId,
  pageGatewayAuth,
  copyText,
  buildSseUrl,
  buildStreamableUrl,
} from '@/api/admin'
import { authBadge, statusBadge, fmtDate, httpMethodBadge } from '@/utils/format'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const id = computed(() => decodeURIComponent(route.params.id))
const gateway = ref(null)
const tools = ref([])
const protocols = ref([])
const auths = ref([])
const loading = ref(true)

async function loadAll() {
  loading.value = true
  const cfg = await listGatewayConfig()
  gateway.value = (cfg || []).find(g => g.gatewayId === id.value) || null
  const settled = await Promise.allSettled([
    listGatewayToolByGatewayId(id.value),
    listGatewayProtocolByGatewayId(id.value),
    pageGatewayAuth({ gatewayId: id.value, page: 1, rows: 50 }),
  ])
  tools.value     = (settled[0].status === 'fulfilled' ? settled[0].value : [])
                    .map((r, i) => ({ ...r, _rowKey: `${r.toolId}-${i}` }))
  protocols.value = (settled[1].status === 'fulfilled' ? settled[1].value : [])
                    .map((r, i) => ({ ...r, _rowKey: `${r.protocolId}-${i}` }))
  // page 接口 unpack 后是 { list, total } —— 兼容两种形态
  const authPage = settled[2].status === 'fulfilled' ? settled[2].value : null
  const authRows = authPage && Array.isArray(authPage) ? authPage
                : (authPage && Array.isArray(authPage.list) ? authPage.list : [])
  auths.value = authRows.map((r, i) => ({ ...r, _rowKey: `${r.gatewayId}-${i}` }))
  loading.value = false
}

onMounted(loadAll)

function endpointUrl(transport) {
  if (transport === 'streamable') {
    return gateway.value?.streamableUrl || buildStreamableUrl(id.value)
  }
  return gateway.value?.sseUrl || buildSseUrl(id.value)
}

async function copyEndpoint(transport) {
  const label = transport === 'streamable' ? 'Streamable' : 'SSE'
  try {
    await copyText(endpointUrl(transport))
    toast.success(`${label} 地址已复制`, { duration: 1800 })
  } catch {
    toast.error('复制失败', { duration: 1800 })
  }
}

function back() { router.push('/gateways') }
function gotoProtocol(row) { router.push(`/protocols/${row.protocolId}`) }

const authTone = computed(() => authBadge(gateway.value?.auth).tone || 'default')
const statusTone = computed(() => statusBadge(gateway.value?.status).tone || 'default')
</script>

<template>
  <div class="gw-detail">
    <header class="hero">
      <div class="hero-left">
        <div class="gw-avatar">
          {{ (gateway?.gatewayName || id).slice(0,1).toUpperCase() }}
        </div>
        <div>
          <span class="eyebrow">Gateway · {{ id }}</span>
          <h1>{{ gateway?.gatewayName || '—' }}</h1>
          <div class="hero-meta">
            <StatusPill :tone="authTone">auth · {{ authBadge(gateway?.auth).label }}</StatusPill>
            <StatusPill :tone="statusTone">status · {{ statusBadge(gateway?.status).label }}</StatusPill>
            <StatusPill tone="violet">v{{ gateway?.version || '1.0.0' }}</StatusPill>
          </div>
        </div>
      </div>
      <div class="hero-right">
        <button class="btn btn-secondary" @click="copyEndpoint('sse')">
          <el-icon><CopyDocument /></el-icon> 复制 SSE URL
        </button>
        <button class="btn btn-secondary" @click="copyEndpoint('streamable')">
          <el-icon><CopyDocument /></el-icon> 复制 Streamable URL
        </button>
        <button class="btn btn-primary" @click="back">
          <el-icon><Back /></el-icon> 返回列表
        </button>
      </div>
    </header>

    <PageCard eyebrow="Summary" :title="gateway?.gatewayDesc || '暂无描述'" desc="这是一个网关的核心元数据">
      <div class="kv-grid">
        <div class="kv card-hover"><span>网关 ID</span><code>{{ id }}</code></div>
        <div class="kv card-hover"><span>认证</span><span>{{ gateway?.auth === 1 ? '启用' : '禁用' }}</span></div>
        <div class="kv card-hover"><span>校验强度</span><span>{{ gateway?.status === 1 ? '强校验' : '不校验' }}</span></div>
        <div class="kv card-hover"><span>协议数</span><span>{{ protocols.length }}</span></div>
        <div class="kv card-hover"><span>工具数</span><span>{{ tools.length }}</span></div>
        <div class="kv card-hover"><span>API Key</span><span>{{ auths.length ? '已配置' : '未配置' }}</span></div>
      </div>
    </PageCard>

    <PageCard eyebrow="Tools" title="绑定的网关工具" desc="每个工具都可以关联一条 HTTP 协议">
      <ElGrid
        :loading="loading"
        :data="tools"
        :show-pager="false"
        empty-icon="Tools"
        empty-title="还没有工具"
        empty-desc="去「网关工具」页给该网关绑定一个工具"
      >
        <el-table-column prop="toolId" label="Tool ID" min-width="180">
          <template #default="{ row }"><IdCell :value="row.toolId" :max="40" /></template>
        </el-table-column>
        <el-table-column prop="toolName" label="名称" min-width="200">
          <template #default="{ row }">
            <span class="cell-strong" :title="row.toolName || '—'">{{ row.toolName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="toolType" label="类型" width="100" align="center">
          <template #default="{ row }"><StatusPill tone="info">{{ row.toolType }}</StatusPill></template>
        </el-table-column>
        <el-table-column prop="toolVersion" label="版本" width="100" align="center">
          <template #default="{ row }"><span class="ver-pill" :title="row.toolVersion ? `v${row.toolVersion}` : ''">v{{ row.toolVersion }}</span></template>
        </el-table-column>
        <el-table-column prop="protocolId" label="协议" width="160">
          <template #default="{ row }">
            <IdCell v-if="row.protocolId" :value="row.protocolId" :max="40" />
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="toolDescription" label="描述" min-width="220">
          <template #default="{ row }">
            <span class="muted" :title="row.toolDescription || '—'">{{ row.toolDescription || '—' }}</span>
          </template>
        </el-table-column>
      </ElGrid>
    </PageCard>

    <PageCard eyebrow="Protocols" title="协议列表" desc="网关可直接调用的 HTTP 接口">
      <ElGrid
        :data="protocols"
        :show-pager="false"
        empty-icon="Share"
        empty-title="还没有协议"
        empty-desc="去「协议配置」页新增 / 导入一条协议"
      >
        <el-table-column prop="protocolId" label="ID" min-width="180">
          <template #default="{ row }"><IdCell :value="row.protocolId" :max="40" /></template>
        </el-table-column>
        <el-table-column prop="httpUrl" label="URL" min-width="240">
          <template #default="{ row }">
            <code class="url" :title="row.httpUrl">{{ row.httpUrl }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="httpMethod" label="方法" width="110" align="center">
          <template #default="{ row }">
            <StatusPill :tone="httpMethodBadge(row.httpMethod).tone">{{ httpMethodBadge(row.httpMethod).label }}</StatusPill>
          </template>
        </el-table-column>
        <el-table-column prop="timeout" label="超时" width="120">
          <template #default="{ row }">
            <KeyValueCell k="超时" :v="row.timeout != null ? `${row.timeout} ms` : '—'" />
          </template>
        </el-table-column>
        <el-table-column prop="httpHeaders" label="Headers" min-width="220">
          <template #default="{ row }">
            <HeaderChipsCell :raw="row.httpHeaders" :max="3" />
          </template>
        </el-table-column>
        <el-table-column prop="mappings" label="参数映射" width="120" align="center">
          <template #default="{ row }">
            <StatusPill tone="violet">{{ row.mappings?.length || 0 }} 项</StatusPill>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="right">
          <template #default="{ row }">
            <button class="op-btn" @click="gotoProtocol(row)">
              <el-icon><View /></el-icon> 详情
            </button>
          </template>
        </el-table-column>
      </ElGrid>
    </PageCard>

    <PageCard eyebrow="Auth" title="认证与限流" desc="该网关的 API Key、限流速率、过期时间">
      <ElGrid
        :data="auths"
        :show-pager="false"
        empty-icon="Key"
        empty-title="还没有认证配置"
        empty-desc="去「认证限流」页新增一条认证"
      >
        <el-table-column prop="apiKey" label="API Key" min-width="280">
          <template #default="{ row }">
            <IdCell
              :value="row.apiKey"
              :truncate="false"
              tone="warn"
              toast-msg="API Key 已复制"
            />
          </template>
        </el-table-column>
        <el-table-column prop="rateLimit" label="速率" width="190">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.rateLimit != null"
              placement="top"
              :show-after="120"
              :content="`每小时 ${row.rateLimit} 次;后端按 3600 平摊,实际限流约 ${(row.rateLimit / 3600).toFixed(4)} 次/秒`"
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
        <el-table-column prop="expireTime" label="过期时间" min-width="220">
          <template #default="{ row }">
            <KeyValueCell k="到期" :v="row.expireTime == null ? '永不过期' : fmtDate(row.expireTime)" />
          </template>
        </el-table-column>
      </ElGrid>
    </PageCard>
  </div>
</template>

<style scoped>
.gw-detail {
  display: flex;
  flex-direction: column;
  gap: 22px;
  animation: fadeIn 0.3s ease-out;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
  padding: 4px;
}
.hero-left {
  display: flex;
  align-items: center;
  gap: 18px;
}
.gw-avatar {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-xl);
  background: var(--gradient-primary);
  color: #ffffff;
  font-weight: 800;
  font-size: 24px;
  display: grid;
  place-items: center;
  box-shadow: var(--shadow-glow);
}
.hero-left h1 {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.025em;
  margin-top: 8px;
  color: var(--text-strong);
}
.hero-meta {
  display: flex;
  gap: 6px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.hero-right { display: flex; gap: 10px; flex-shrink: 0; }

.kv-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
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
  box-shadow: var(--shadow-sm);
}
.kv span:first-child {
  font-size: 11px;
  letter-spacing: 0.02em;
  color: var(--text-faint);
  font-weight: 500;
}
.kv span:last-child,
.kv code {
  font-size: 13px;
  color: var(--text-strong);
  font-family: 'JetBrains Mono', monospace;
  font-weight: 500;
}

/* ===== table cell 字体对齐 —— 让 sub2api 风格表格里的纯文本好看 ===== */
:deep(.cell-strong) {
  font-weight: var(--fw-semibold);
  color: var(--text-strong);
  font-size: var(--fs-base);
  line-height: 1.3;
  letter-spacing: var(--ls-snug);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  display: inline-block;
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
}
:deep(.muted) { color: var(--text-muted); font-size: 12.5px; }
:deep(.url)   { font-family: 'JetBrains Mono', monospace; font-size: 12px; }
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

:deep(.op-btn) {
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
    transform 220ms var(--ease-spring);
  white-space: nowrap;
}
:deep(.op-btn:hover) {
  color: var(--primary-600);
  border-color: var(--primary-300);
  background: var(--primary-100);
  transform: translateY(-1px);
}
:root.dark :deep(.op-btn) { border-color: var(--hairline-soft); color: var(--text-muted); }
:root.dark :deep(.op-btn:hover) {
  color: var(--primary-300);
  border-color: rgba(20, 184, 166, 0.45);
  background: rgba(20, 184, 166, 0.10);
}

@media (max-width: 720px) {
  .kv-grid { grid-template-columns: 1fr 1fr; }
  .hero { flex-direction: column; align-items: flex-start; }
}
</style>
