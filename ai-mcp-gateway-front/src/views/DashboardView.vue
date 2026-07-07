<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import StatCard from '@/components/ui/StatCard.vue'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import IdCell from '@/components/ui/IdCell.vue'
import {
  listGatewayConfig,
  listGatewayTool,
  listGatewayProtocol,
  listGatewayAuth,
  buildSseUrl,
  copyText,
} from '@/api/admin'
import { authBadge, statusBadge, fmtDate } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const API_BASE = import.meta.env.VITE_API_BASE || '/admin'
const APP_MODE = (import.meta.env.MODE || 'development').toUpperCase()

const loading = ref(true)
const gateways = ref([])
const tools = ref([])
const protocols = ref([])
const auths = ref([])

async function loadAll() {
  loading.value = true
  const settled = await Promise.allSettled([
    listGatewayConfig(),
    listGatewayTool(),
    listGatewayProtocol(),
    listGatewayAuth(),
  ])
  gateways.value  = settled[0].status === 'fulfilled' ? settled[0].value : []
  tools.value     = settled[1].status === 'fulfilled' ? settled[1].value : []
  protocols.value = settled[2].status === 'fulfilled' ? settled[2].value : []
  auths.value     = settled[3].status === 'fulfilled' ? settled[3].value : []
  loading.value = false
}

onMounted(loadAll)

const authEnabledCount = computed(() => gateways.value.filter(g => g.auth === 1).length)
const strongValidCount = computed(() => gateways.value.filter(g => g.status === 1).length)

const recent = computed(() => gateways.value.slice(0, 5))

function go(path) {
  router.push(path)
}

const now = computed(() =>
  new Date().toLocaleString('zh-CN', { hour12: false })
)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6)  return '夜深了'
  if (h < 12) return '上午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

async function quickCopySse(gatewayId) {
  await copyText(buildSseUrl(gatewayId))
}
</script>

<template>
  <div class="dashboard">
    <!-- Greeting strip -->
    <div class="greeting">
      <div class="greet-left">
        <span class="eyebrow">
          {{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year:'numeric', month:'long', day:'numeric' }) }}
        </span>
        <h1 class="greet-title">
          {{ greeting }}, <span class="text-accent">{{ auth.user?.nickname || 'Admin' }}</span>
        </h1>
        <p class="greet-desc">
          这里是 MCP 网关运营的总览。
          <span v-if="loading">正在拉取实时数据…</span>
          <span v-else>当前共 <b>{{ gateways.length }}</b> 个网关 · <b>{{ tools.length }}</b> 个工具 · <b>{{ protocols.length }}</b> 条协议。</span>
        </p>
      </div>
      <div class="greet-right">
        <button class="btn" @click="go('/protocols')">
          <el-icon><Upload /></el-icon> 导入 OpenAPI
        </button>
        <button class="btn btn--primary" @click="go('/gateways')">
          <el-icon><Plus /></el-icon> 新建网关
        </button>
      </div>
    </div>

    <!-- Stat row -->
    <section class="stat-row">
      <StatCard title="网关总数"     :value="gateways.length"       tone="blue"   icon="Connection" :hint="`启用认证: ${authEnabledCount}`" />
      <StatCard title="协议配置"     :value="protocols.length"      tone="violet" icon="Share"      hint="可绑定到任意网关" />
      <StatCard title="网关工具"     :value="tools.length"          tone="mint"   icon="Tools"      hint="每个工具可挂一个协议" />
      <StatCard title="认证限流"     :value="auths.length"          tone="amber"  icon="Key"        :hint="`强校验网关: ${strongValidCount}`" />
    </section>

    <!-- Bento: 主内容 + 侧栏 -->
    <section class="bento">
      <PageCard eyebrow="Recent" title="最近活跃的网关" desc="按列表顺序取前 5 条,点击操作即可进入管理">
        <template #actions>
          <button class="btn btn--sm btn--ghost" @click="go('/gateways')">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </button>
        </template>

        <div class="recent-list">
          <div v-if="!loading && recent.length === 0" class="empty-host">
            <EmptyState icon="Connection" title="还没有网关" desc="在「网关列表」中创建第一个网关,即可在这里看到概览。" />
          </div>
          <ul v-else class="recent-ul">
            <li v-for="g in recent" :key="g.gatewayId" class="recent-item">
              <div class="recent-meta">
                <div class="recent-gw">
                  <div class="gw-avatar">
                    {{ (g.gatewayName || g.gatewayId || '?').slice(0, 1).toUpperCase() }}
                  </div>
                  <div class="gw-meta-col">
                    <div class="gw-name">{{ g.gatewayName || g.gatewayId }}</div>
                    <IdCell :value="g.gatewayId" />
                  </div>
                </div>
                <span class="recent-desc">{{ g.gatewayDesc || '—' }}</span>
              </div>

              <div class="recent-tags">
                <StatusPill :tone="authBadge(g.auth).tone">{{ authBadge(g.auth).label }}</StatusPill>
                <StatusPill :tone="statusBadge(g.status).tone">{{ statusBadge(g.status).label }}</StatusPill>
                <StatusPill tone="violet">v{{ g.version || '1.0.0' }}</StatusPill>
              </div>

              <div class="recent-actions">
                <el-tooltip content="复制 SSE 地址" placement="top">
                  <button class="icon-btn" @click="quickCopySse(g.gatewayId)">
                    <el-icon><CopyDocument /></el-icon>
                  </button>
                </el-tooltip>
                <button class="btn btn--sm" @click="go(`/gateways/${g.gatewayId}`)">
                  <el-icon><View /></el-icon> 详情
                </button>
              </div>
            </li>
          </ul>
        </div>
      </PageCard>

      <!-- 右侧栏 -->
      <div class="bento-side">
        <PageCard eyebrow="System" title="环境状态" desc="来自后端的连接状况">
          <div class="env-grid">
            <div class="env-item">
              <span class="env-label">API 端点</span>
              <code class="env-value">{{ API_BASE }}</code>
            </div>
            <div class="env-item">
              <span class="env-label">前端版本</span>
              <span class="env-value">v1.0.0 · Glacis</span>
            </div>
            <div class="env-item">
              <span class="env-label">运行模式</span>
              <span class="env-value">{{ APP_MODE }}</span>
            </div>
            <div class="env-item">
              <span class="env-label">最近刷新</span>
              <span class="env-value">{{ now }}</span>
            </div>
          </div>
        </PageCard>

        <PageCard eyebrow="Quickstart" title="常用操作">
          <div class="quick-actions">
            <button class="qa" @click="go('/gateways')">
              <el-icon><Connection /></el-icon>
              <span>新建网关</span>
            </button>
            <button class="qa" @click="go('/tools')">
              <el-icon><Tools /></el-icon>
              <span>绑定工具</span>
            </button>
            <button class="qa" @click="go('/protocols')">
              <el-icon><Upload /></el-icon>
              <span>导入协议</span>
            </button>
            <button class="qa" @click="go('/auth')">
              <el-icon><Key /></el-icon>
              <span>发放 API Key</span>
            </button>
          </div>
        </PageCard>
      </div>
    </section>

    <PageCard eyebrow="Activity" title="最近接入的协议" desc="从协议表按列表顺序取前 8 条,仅展示概览">
      <ul class="protocol-mini">
        <li v-for="p in protocols.slice(0, 8)" :key="p.protocolId" class="protocol-mini-item">
          <IdCell :value="p.protocolId" />
          <code class="url">{{ p.httpUrl }}</code>
          <StatusPill :tone="(p.httpMethod || '').toUpperCase() === 'GET' ? 'info' : 'success'">{{ p.httpMethod }}</StatusPill>
          <span class="meta">{{ p.timeout ?? '-' }} ms</span>
          <StatusPill tone="violet">{{ p.mappings?.length || 0 }} 项</StatusPill>
        </li>
        <li v-if="!loading && protocols.length === 0" class="empty-host">
          <EmptyState icon="Share" title="还没有协议" desc="导入一份 OpenAPI,或新增一条协议。" />
        </li>
      </ul>
    </PageCard>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

/* ===== Greeting ===== */
.greeting {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  padding: 4px 4px;
}

.greet-title {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.025em;
  margin-top: 12px;
}

.greet-desc {
  margin-top: 8px;
  color: var(--text-muted);
  font-size: 13.5px;

  b {
    color: var(--text-strong);
    font-feature-settings: 'tnum';
    font-weight: 700;
    margin: 0 2px;
  }
}

.greet-right {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

/* ===== Stat row ===== */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1080px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 540px) {
  .stat-row { grid-template-columns: 1fr; }
}

/* ===== Bento ===== */
.bento {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}

@media (max-width: 1080px) {
  .bento { grid-template-columns: 1fr; }
}

.recent-list { min-height: 200px; }

.empty-host { padding: 24px 0; }

.recent-ul {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-item {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 14px;
  align-items: center;
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
  transition: all var(--dur-base) var(--ease);
}

.recent-item:hover {
  background: #ffffff;
  border-color: var(--accent-line);
  box-shadow: var(--shadow-sm);
}

.recent-meta { min-width: 0; }

.recent-gw {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.gw-avatar {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  display: grid;
  place-items: center;
  color: var(--text-strong);
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.gw-meta-col { display: flex; flex-direction: column; gap: 4px; min-width: 0; }

.gw-name {
  font-weight: 600;
  color: var(--text-strong);
  font-size: 14px;
  letter-spacing: -0.005em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 280px;
}

.recent-desc {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 4px;
  max-width: 320px;
}

.recent-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.recent-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.icon-btn {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  background: #ffffff;
  border: 1px solid var(--hairline);
  border-radius: 8px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--dur-base) var(--ease);
}
.icon-btn:hover {
  background: var(--bg-deep);
  color: var(--text-strong);
  border-color: var(--hairline-strong);
}

/* ===== Side ===== */
.bento-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.env-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.env-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 9px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
}

.env-label {
  font-size: 11px;
  letter-spacing: 0.02em;
  color: var(--text-faint);
  font-weight: 500;
}

.env-value {
  font-size: 12.5px;
  color: var(--text-strong);
  font-family: 'JetBrains Mono', monospace;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.qa {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  height: auto;
  border-radius: 9px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
  color: var(--text-default);
  cursor: pointer;
  text-align: left;
  transition: all var(--dur-base) var(--ease);
}

.qa:hover {
  background: #ffffff;
  border-color: var(--accent-line);
  color: var(--text-strong);
  box-shadow: var(--shadow-xs);
  transform: translateY(-1px);
}

.qa span {
  font-size: 13px;
  font-weight: 500;
}

.qa .el-icon {
  font-size: 18px;
  color: var(--accent);
}

/* ===== Protocol mini list (replaces table on dashboard) ===== */
.protocol-mini {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.protocol-mini-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto auto;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 8px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
  transition: all var(--dur-base) var(--ease);
}

.protocol-mini-item:hover {
  background: #ffffff;
  border-color: var(--hairline-strong);
}

.protocol-mini-item .url {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12.5px;
  color: var(--text-default);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.protocol-mini-item .meta {
  font-size: 11.5px;
  color: var(--text-faint);
  font-family: 'JetBrains Mono', monospace;
}
</style>