<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import IdCell from '@/components/ui/IdCell.vue'
import VxeGrid from '@/components/ui/VxeGrid.vue'
import {
  listGatewayConfig,
  listGatewayToolByGatewayId,
  listGatewayProtocolByGatewayId,
  pageGatewayAuth,
  copyText,
  buildSseUrl,
} from '@/api/admin'
import { authBadge, statusBadge, fmtDate, httpMethodBadge } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const id = computed(() => decodeURIComponent(route.params.id))
const gateway = ref(null)
const tools = ref([])
const protocols = ref([])
const auths = ref([])
const loading = ref(true)

async function loadAll() {
  loading.value = true
  const data = await listGatewayConfig()
  gateway.value = (data || []).find(g => g.gatewayId === id.value) || null
  const settled = await Promise.allSettled([
    listGatewayToolByGatewayId(id.value),
    listGatewayProtocolByGatewayId(id.value),
    pageGatewayAuth({ gatewayId: id.value, page: 1, rows: 10 }),
  ])
  tools.value     = (settled[0].status === 'fulfilled' ? settled[0].value : [])
                    .map((r, i) => ({ ...r, _rowKey: `${r.toolId}-${i}` }))
  protocols.value = (settled[1].status === 'fulfilled' ? settled[1].value : [])
                    .map((r, i) => ({ ...r, _rowKey: `${r.protocolId}-${i}` }))
  auths.value     = (settled[2].status === 'fulfilled' ? settled[2].value.list || [] : [])
                    .map((r, i) => ({ ...r, _rowKey: `${r.gatewayId}-${i}` }))
  loading.value = false
}

onMounted(loadAll)

async function copySse() {
  await copyText(buildSseUrl(id.value))
  ElMessage.success('SSE 地址已复制')
}

function back() {
  router.push('/gateways')
}

function gotoProtocol(row) {
  router.push(`/protocols/${row.protocolId}`)
}

const authTone = computed(() => authBadge(gateway.value?.auth).tone || 'default')
const statusTone = computed(() => statusBadge(gateway.value?.status).tone || 'default')
</script>

<template>
  <div class="gw-detail">
    <!-- Header strip -->
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
        <button class="btn" @click="copySse">
          <el-icon><CopyDocument /></el-icon> 复制 SSE URL
        </button>
        <button class="btn btn--primary" @click="back">
          <el-icon><Back /></el-icon> 返回列表
        </button>
      </div>
    </header>

    <!-- Description -->
    <PageCard eyebrow="Summary" :title="gateway?.gatewayDesc || '暂无描述'" desc="这是一个网关的核心元数据">
      <div class="kv-grid">
        <div class="kv"><span>网关 ID</span><code>{{ id }}</code></div>
        <div class="kv"><span>认证</span><span>{{ gateway?.auth === 1 ? '启用' : '禁用' }}</span></div>
        <div class="kv"><span>校验强度</span><span>{{ gateway?.status === 1 ? '强校验' : '不校验' }}</span></div>
        <div class="kv"><span>协议数</span><span>{{ protocols.length }}</span></div>
        <div class="kv"><span>工具数</span><span>{{ tools.length }}</span></div>
        <div class="kv"><span>API Key</span><span>{{ auths.length ? '已配置' : '未配置' }}</span></div>
      </div>
    </PageCard>

    <!-- Tools -->
    <PageCard eyebrow="Tools" title="绑定的网关工具" desc="每个工具都可以关联一条 HTTP 协议">
      <div class="grid-host">
        <VxeGrid
          :loading="loading"
          :data="tools"
          :show-pager="false"
          empty-icon="Tools"
          empty-title="还没有工具"
          empty-desc="去「网关工具」页给该网关绑定一个工具"
        >
          <vxe-column field="toolId" title="Tool ID" width="160">
            <template #default="{ row }"><IdCell :value="row.toolId" /></template>
          </vxe-column>
          <vxe-column field="toolName" title="名称" min-width="200" show-overflow="tooltip" />
          <vxe-column field="toolType" title="类型" width="120">
            <template #default="{ row }"><StatusPill tone="info">{{ row.toolType }}</StatusPill></template>
          </vxe-column>
          <vxe-column field="toolVersion" title="版本" width="120">
            <template #default="{ row }"><StatusPill tone="violet">v{{ row.toolVersion }}</StatusPill></template>
          </vxe-column>
          <vxe-column field="protocolId" title="协议" width="140">
            <template #default="{ row }">
              <code v-if="row.protocolId">#{{ row.protocolId }}</code>
              <span v-else class="muted">—</span>
            </template>
          </vxe-column>
          <vxe-column field="toolDescription" title="描述" min-width="220" show-overflow="tooltip">
            <template #default="{ row }">
              <span class="muted">{{ row.toolDescription || '—' }}</span>
            </template>
          </vxe-column>
        </VxeGrid>
      </div>
    </PageCard>

    <!-- Protocols -->
    <PageCard eyebrow="Protocols" title="协议列表" desc="网关可直接调用的 HTTP 接口">
      <div class="grid-host">
        <VxeGrid
          :data="protocols"
          :show-pager="false"
          empty-icon="Share"
          empty-title="还没有协议"
          empty-desc="去「协议配置」页新增 / 导入一条协议"
        >
          <vxe-column field="protocolId" title="ID" width="120">
            <template #default="{ row }"><IdCell :value="row.protocolId" /></template>
          </vxe-column>
          <vxe-column field="httpUrl" title="URL" min-width="240" show-overflow="tooltip">
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
          <vxe-column field="mappings" title="参数映射" width="120">
            <template #default="{ row }">
              <StatusPill tone="violet">{{ row.mappings?.length || 0 }} 项</StatusPill>
            </template>
          </vxe-column>
          <vxe-column title="操作" width="120" align="right">
            <template #default="{ row }">
              <button class="btn btn--sm btn--ghost" @click="gotoProtocol(row)">
                <el-icon><View /></el-icon> 详情
              </button>
            </template>
          </vxe-column>
        </VxeGrid>
      </div>
    </PageCard>

    <!-- Auth -->
    <PageCard eyebrow="Auth" title="认证与限流" desc="该网关的 API Key、限流速率、过期时间">
      <div class="grid-host">
        <VxeGrid
          :data="auths"
          :show-pager="false"
          empty-icon="Key"
          empty-title="还没有认证配置"
          empty-desc="去「认证限流」页新增一条认证"
        >
          <vxe-column field="apiKey" title="API Key" min-width="280">
            <template #default="{ row }">
              <code class="url">{{ row.apiKey }}</code>
            </template>
          </vxe-column>
          <vxe-column field="rateLimit" title="速率" width="160">
            <template #default="{ row }">{{ row.rateLimit }} 次/秒</template>
          </vxe-column>
          <vxe-column field="expireTime" title="过期时间" min-width="240">
            <template #default="{ row }">{{ fmtDate(row.expireTime) }}</template>
          </vxe-column>
        </VxeGrid>
      </div>
    </PageCard>
  </div>
</template>

<style scoped lang="scss">
.gw-detail {
  display: flex;
  flex-direction: column;
  gap: 22px;
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
  width: 60px;
  height: 60px;
  border-radius: 14px;
  background: var(--accent);
  color: #ffffff;
  font-weight: 800;
  font-size: 24px;
  display: grid;
  place-items: center;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}

.hero-left h1 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-top: 8px;
}

.hero-meta {
  display: flex;
  gap: 6px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.hero-right {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

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
  border-radius: 9px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
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

.muted { color: var(--text-muted); font-size: 12.5px; }
.url   { font-family: 'JetBrains Mono', monospace; font-size: 12px; }

.grid-host {
  border: 1px solid var(--hairline);
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
}

@media (max-width: 720px) {
  .kv-grid { grid-template-columns: 1fr 1fr; }
  .hero { flex-direction: column; align-items: flex-start; }
}
</style>