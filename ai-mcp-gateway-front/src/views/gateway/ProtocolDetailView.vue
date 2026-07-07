<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import IdCell from '@/components/ui/IdCell.vue'
import VxeGrid from '@/components/ui/VxeGrid.vue'
import {
  listGatewayProtocol,
  pageGatewayProtocol,
  copyText,
} from '@/api/admin'
import { fmtDate, httpMethodBadge } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const id = computed(() => route.params.id)
const protocol = ref(null)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    let target = null
    try {
      const { list } = await pageGatewayProtocol({ protocolId: String(id.value), page: 1, rows: 5 })
      target = (list || []).find(p => String(p.protocolId) === String(id.value))
    } catch {}
    if (!target) {
      const list = await listGatewayProtocol()
      target = (list || []).find(p => String(p.protocolId) === String(id.value))
    }
    protocol.value = target || null
    if (target && target.mappings) {
      target.mappings = (target.mappings || []).map((m, i) => ({ ...m, _rowKey: `${m.fieldName}-${i}` }))
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)

function back() {
  router.push('/protocols')
}

async function copyUrl(u) {
  await copyText(u)
  ElMessage.success('已复制')
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
        <button class="btn" @click="copyUrl(protocol?.httpUrl)">
          <el-icon><CopyDocument /></el-icon> 复制 URL
        </button>
        <button class="btn btn--primary" @click="back">
          <el-icon><Back /></el-icon> 返回
        </button>
      </div>
    </header>

    <div v-if="loading" class="loading-state">
      <div class="spin" /> 加载中…
    </div>

    <template v-else-if="protocol">
      <PageCard eyebrow="HTTP" title="请求信息" desc="发起该协议时的基础字段">
        <div class="kv-grid">
          <div class="kv"><span>请求方法</span><StatusPill :tone="httpMethodBadge(protocol.httpMethod).tone">{{ protocol.httpMethod }}</StatusPill></div>
          <div class="kv"><span>超时</span><b>{{ protocol.timeout ?? '-' }} ms</b></div>
          <div class="kv"><span>Headers</span><code>{{ protocol.httpHeaders || '{}' }}</code></div>
          <div class="kv span-2"><span>URL</span><code class="url">{{ protocol.httpUrl }}</code></div>
        </div>
      </PageCard>

      <PageCard eyebrow="Mappings" title="参数映射" :desc="`共 ${protocol.mappings?.length || 0} 项字段映射`">
        <div class="grid-host">
          <VxeGrid
            :data="protocol.mappings || []"
            :show-pager="false"
            empty-icon="Connection"
            empty-title="没有参数映射"
            empty-desc="该协议尚未配置字段映射"
          >
            <vxe-column field="mappingType" title="类型" width="120">
              <template #default="{ row }">
                <StatusPill :tone="row.mappingType === 'request' ? 'info' : 'success'">{{ row.mappingType }}</StatusPill>
              </template>
            </vxe-column>
            <vxe-column field="parentPath" title="父级" width="180">
              <template #default="{ row }">
                <code v-if="row.parentPath">{{ row.parentPath }}</code>
                <span v-else class="muted">—</span>
              </template>
            </vxe-column>
            <vxe-column field="fieldName" title="字段名" min-width="160">
              <template #default="{ row }"><b>{{ row.fieldName }}</b></template>
            </vxe-column>
            <vxe-column field="mcpPath" title="MCP Path" min-width="200" show-overflow="tooltip">
              <template #default="{ row }"><code>{{ row.mcpPath }}</code></template>
            </vxe-column>
            <vxe-column field="mcpType" title="MCP 类型" width="130">
              <template #default="{ row }"><StatusPill tone="violet">{{ row.mcpType }}</StatusPill></template>
            </vxe-column>
            <vxe-column field="isRequired" title="必填" width="100">
              <template #default="{ row }">
                <StatusPill :tone="row.isRequired === 1 ? 'danger' : 'default'">
                  {{ row.isRequired === 1 ? '是' : '否' }}
                </StatusPill>
              </template>
            </vxe-column>
            <vxe-column field="mcpDesc" title="描述" min-width="180" show-overflow="tooltip">
              <template #default="{ row }"><span class="muted">{{ row.mcpDesc || '—' }}</span></template>
            </vxe-column>
            <vxe-column field="sortOrder" title="排序" width="80">
              <template #default="{ row }">{{ row.sortOrder ?? '-' }}</template>
            </vxe-column>
          </VxeGrid>
        </div>
      </PageCard>
    </template>

    <PageCard v-else eyebrow="Not Found" title="协议不存在" desc="它可能已被删除,或 ID 输入有误。" />
  </div>
</template>

<style scoped lang="scss">
.pd { display: flex; flex-direction: column; gap: 22px; }

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
}

.hero-left .url {
  font-family: 'JetBrains Mono', monospace;
  font-size: 16px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  padding: 6px 12px;
  border-radius: 9px;
  color: var(--text-strong);
}

.hero-right { display: flex; gap: 10px; flex-shrink: 0; }

.kv-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
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
.kv.span-2 { grid-column: 1 / -1; }
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

.muted { color: var(--text-muted); font-size: 12.5px; }

.grid-host {
  border: 1px solid var(--hairline);
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
}

.loading-state {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  padding: 80px 16px;
  color: var(--text-muted);
}
.spin {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2.5px solid var(--accent-soft);
  border-top-color: var(--accent);
  animation: spin 720ms linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 720px) {
  .kv-grid { grid-template-columns: 1fr; }
  .hero { flex-direction: column; align-items: flex-start; }
}
</style>