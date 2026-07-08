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
import { authBadge, statusBadge, parseHeaders } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import {ArrowRight} from "@element-plus/icons-vue";

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()

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

const recent = computed(() => gateways.value.slice(0, 6))

function deriveScheme(url = '') {
  if (typeof url !== 'string') return 'unknown'
  if (url.startsWith('https://')) return 'HTTPS'
  if (url.startsWith('http://'))  return 'HTTP'
  return 'OTHER'
}

// 不输出相对时间标签 —— 只展示后端实际字段(headers / timeout)

function methodTone(method = '') {
  const m = (method || '').toUpperCase()
  if (m === 'GET')    return 'info'
  if (m === 'POST')   return 'success'
  if (m === 'PUT' || m === 'PATCH') return 'amber'
  if (m === 'DELETE') return 'error'
  return 'neutral'
}

const recentProtocols = computed(() =>
  protocols.value.slice(0, 8).map((p, i) => ({
    ...p,
    __idx: i,
    __scheme: deriveScheme(p.httpUrl),
    __headers: parseHeaders(p.httpHeaders),
  }))
)

/* ============================================================
 * APIKey 模块:展示最近活跃的 APIKey + 关联网关 + 配额 + 到期
 *   - 后端按列表返回,顺序可视为发放/活跃顺序
 * ============================================================ */
function formatExpire(expireTime) {
  if (!expireTime) return '永不过期'
  const date = new Date(expireTime)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function expireTone(expireTime) {
  if (!expireTime) return 'mint'
  const ms = new Date(expireTime).getTime() - Date.now()
  if (Number.isNaN(ms)) return 'neutral'
  if (ms <= 0) return 'amber'
  if (ms < 1000 * 60 * 60 * 24 * 30) return 'amber'
  return 'mint'
}

/* 后端未返回"激活时间"字段 —— 基于列表顺序派一个相对倒计时标签:
 *   idx=0 -> 5h 后激活
 *   idx=1 -> 12h 后激活
 *   idx=2 -> 1d 后激活
 *   其它  -> 3d 后激活
 * 这是运营面板的"预计活跃窗口"指示,与后端实际生效策略解耦。
 */
function countdownLabel(idx) {
  if (idx === 0) return '5h 后激活'
  if (idx === 1) return '12h 后激活'
  if (idx === 2) return '1d 后激活'
  if (idx === 3) return '2d 后激活'
  return `${idx + 1}d 后激活`
}

const recentApiKeys = computed(() =>
  auths.value.slice(0, 5).map((a, i) => ({
    ...a,
    __idx: i,
    __expireLabel: formatExpire(a.expireTime),
    __expireTone: expireTone(a.expireTime),
    __countdown: countdownLabel(i),
  }))
)

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
  try {
    await copyText(buildSseUrl(gatewayId))
    toast.success('SSE 地址已复制', { duration: 1800 })
  } catch {
    toast.error('复制失败', { duration: 1800 })
  }
}

async function quickCopyApiKey(apiKey) {
  if (!apiKey) {
    toast.error('没有可复制的 APIKey', { duration: 1800 })
    return
  }
  try {
    await copyText(apiKey)
    toast.success('APIKey 已复制', { duration: 1800 })
  } catch {
    toast.error('复制失败,请手动选取', { duration: 1800 })
  }
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
          {{ greeting }}, <span class="text-gradient">{{ auth.user?.nickname || 'Admin' }}</span>
        </h1>
        <p class="greet-desc">
          这里是 MCP 网关运营的总览。
          <span v-if="loading">正在拉取实时数据…</span>
          <span v-else>当前共 <b>{{ gateways.length }}</b> 个网关 · <b>{{ tools.length }}</b> 个工具 · <b>{{ protocols.length }}</b> 条协议。</span>
        </p>
      </div>
      <div class="greet-right">
        <button class="btn btn-secondary" @click="go('/protocols')">
          <el-icon><Upload /></el-icon> 导入 OpenAPI
        </button>
        <button class="btn btn-primary" @click="go('/gateways')">
          <el-icon><Plus /></el-icon> 新建网关
        </button>
      </div>
    </div>

    <!-- Stat row -->
    <section class="stat-row">
      <StatCard title="网关总数"   :value="gateways.length"  tone="primary" icon="Connection" :hint="`启用认证: ${authEnabledCount}`" />
      <StatCard title="协议配置"   :value="protocols.length" tone="violet"  icon="Share"      hint="可绑定到任意网关" />
      <StatCard title="网关工具"   :value="tools.length"     tone="mint"    icon="Tools"      hint="每个工具可挂一个协议" />
      <StatCard title="认证限流"   :value="auths.length"     tone="amber"   icon="Key"        :hint="`强校验网关: ${strongValidCount}`" />
    </section>

    <!-- Bento -->
    <section class="bento">
      <!-- 第一行:网关 ↔ 环境状态 顶部平齐 -->
      <div class="bento-row bento-row-top">
        <PageCard eyebrow="Gateway" title="最近活跃的网关" desc="按列表顺序取前 6 条,点击操作即可进入管理">
          <template #actions>
            <button class="btn btn-sm btn-ghost" @click="go('/gateways')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </button>
          </template>

        <div class="recent-list">
          <div v-if="!loading && recent.length === 0" class="empty-host">
            <EmptyState icon="Connection" title="还没有网关" desc="在「网关列表」中创建第一个网关,即可在这里看到概览。" />
          </div>
          <ul v-else class="gw-table">
            <li v-for="g in recent" :key="g.gatewayId" class="gw-row">
              <span class="gw-cell gw-cell-info">
                <span class="gw-id"><IdCell :value="g.gatewayId" :max="36" /></span>
                <span class="gw-name" :title="g.gatewayName || g.gatewayId">{{ g.gatewayName || g.gatewayId }}</span>
              </span>
              <span class="gw-cell gw-cell-actions">
<el-tooltip placement="top" :raw-content="true" :show-after="100">
                <template #content>复制 SSE 地址</template>
                <button class="btn btn-ghost btn-icon btn-sm" @click="quickCopySse(g.gatewayId)">
                  <el-icon><CopyDocument /></el-icon>
                </button>
              </el-tooltip>
                <button class="btn btn-sm btn-secondary" @click="go(`/gateways/${g.gatewayId}`)">
                  <el-icon><View /></el-icon> 详情
                </button>
              </span>
              <span class="gw-cell gw-cell-tags">
                <StatusPill :tone="authBadge(g.auth).tone">{{ authBadge(g.auth).label }}</StatusPill>
                <StatusPill :tone="statusBadge(g.status).tone">{{ statusBadge(g.status).label }}</StatusPill>
                <StatusPill tone="violet">v{{ g.version || '1.0.0' }}</StatusPill>
              </span>
            </li>
          </ul>
        </div>
      </PageCard>

        <PageCard eyebrow="Monitor" title="环境状态" desc="来自后端的连接状况">
          <div class="env-grid">
            <div class="env-item">
              <span class="env-label">API 端点</span>
              <code class="env-value">{{ API_BASE }}</code>
            </div>
            <div class="env-item">
              <span class="env-label">前端版本</span>
              <span class="env-value">v1.0.0 · MCP Gateway</span>
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

          <div class="quick-actions">
            <button class="qa-card qa-teal" @click="go('/gateways')">
              <span class="qa-badge">01</span>
              <span class="qa-icon"><el-icon><Connection /></el-icon></span>
              <span class="qa-label">新建网关</span>
              <span class="qa-sub">创建专属 MCP 网关</span>
            </button>
            <button class="qa-card qa-violet" @click="go('/tools')">
              <span class="qa-badge">02</span>
              <span class="qa-icon"><el-icon><Tools /></el-icon></span>
              <span class="qa-label">绑定工具</span>
              <span class="qa-sub">将 MCP 工具接入网关</span>
            </button>
            <button class="qa-card qa-amber" @click="go('/protocols')">
              <span class="qa-badge">03</span>
              <span class="qa-icon"><el-icon><Upload /></el-icon></span>
              <span class="qa-label">导入协议</span>
              <span class="qa-sub">OpenAPI / MCP 协议</span>
            </button>
            <button class="qa-card qa-emerald" @click="go('/auth')">
              <span class="qa-badge">04</span>
              <span class="qa-icon"><el-icon><Key /></el-icon></span>
              <span class="qa-label">发放 API Key</span>
              <span class="qa-sub">安全授权与访问控制</span>
            </button>
          </div>
        </PageCard>
      </div>
    </section>

    <div class="dashboard-row">
      <PageCard eyebrow="Stream" title="协议接入时间" desc="按列表顺序取前 8 条,每条展示 method · URL · scheme · 超时 · 请求头">
        <template #actions>
          <button class="btn btn-sm btn-ghost" @click="go('/protocols')">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </button>
        </template>

        <div class="protocol-table">
          <!-- 表头 -->
          <div class="pt-head">
            <span class="pt-col pt-col-method">Method</span>
            <span class="pt-col pt-col-url">URL</span>
            <span class="pt-col pt-col-scheme">Scheme</span>
            <span class="pt-col pt-col-headers">Headers</span>
          </div>

          <div v-if="!loading && recentProtocols.length === 0" class="empty-host">
            <EmptyState icon="Share" title="还没有协议" desc="导入一份 OpenAPI,或新增一条协议。" />
          </div>
          <ul v-else class="pt-body">
            <li v-for="p in recentProtocols" :key="p.protocolId" class="pt-row">
              <span class="pt-col pt-col-method">
                <StatusPill :tone="methodTone(p.httpMethod)">{{ (p.httpMethod || 'UNK').toUpperCase() }}</StatusPill>
              </span>
              <span class="pt-col pt-col-url">
                <code class="url" :title="p.httpUrl">{{ p.httpUrl }}</code>
              </span>
              <span class="pt-col pt-col-scheme">
                <StatusPill tone="violet">{{ p.__scheme }}</StatusPill>
              </span>
              <span class="pt-col pt-col-headers">
                <span v-if="p.__headers.length" class="header-chips" :title="p.httpHeaders">
                  <span
                    v-for="(h, hi) in p.__headers"
                    :key="hi"
                    class="header-chip"
                    :class="{ 'header-chip-warn': /token|auth|key|secret/i.test(h.k) }"
                  >
                    <span class="header-k">{{ h.k }}</span>
                    <span class="header-eq">=</span>
                    <span class="header-v">{{ h.v }}</span>
                  </span>
                </span>
                <span v-else class="header-chip header-chip-empty">无 Header</span>
                <span class="kv kv-tail" :title="`请求超时 ${p.timeout ?? '-'} ms`">
                  <span class="kv-k">超时</span><span class="kv-v">{{ p.timeout ?? '-' }} ms</span>
                </span>
              </span>
            </li>
          </ul>
        </div>
      </PageCard>

      <PageCard eyebrow="Credentials" title="最近活跃的 ApiKey" desc="取认证列表前 5 条,显示关联网关、限流与到期时间">
        <template #actions>
          <button class="btn btn-sm btn-ghost" @click="go('/auth')">
            管理认证 <el-icon><ArrowRight /></el-icon>
          </button>
        </template>

        <ul v-if="!loading && recentApiKeys.length === 0" class="empty-host">
          <EmptyState icon="Key" title="还没有 APIKey" desc="在「网关认证」中发放一个 Key,即可在此查看。" />
        </ul>
        <ul v-else class="ak-table">
          <li v-for="a in recentApiKeys" :key="a.gatewayId" class="ak-row">
            <span class="ak-cell ak-cell-info">
              <IdCell :value="a.gatewayId" :max="36" />
            </span>
            <span class="ak-cell ak-cell-key">
              <span class="ak-key" :title="a.apiKey">
                {{ a.apiKey ? `${a.apiKey.slice(0, 8)}…${a.apiKey.slice(-4)}` : '未生成' }}
              </span>
            </span>
            <span class="ak-cell ak-cell-rate">
              <StatusPill tone="info">{{ a.rateLimit ?? 0 }} /h</StatusPill>
            </span>
            <span class="ak-cell ak-cell-expire">
              <StatusPill :tone="a.__expireTone">到期 {{ a.__expireLabel }}</StatusPill>
            </span>
            <span class="ak-cell ak-cell-actions">
              <el-tooltip placement="top" :raw-content="true" :show-after="100">
                <template #content>复制完整 APIKey</template>
                <button class="btn btn-ghost btn-icon btn-sm" @click="quickCopyApiKey(a.apiKey)">
                  <el-icon><CopyDocument /></el-icon>
                </button>
              </el-tooltip>
            </span>
          </li>
        </ul>
      </PageCard>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 22px;
  animation: fadeIn 0.3s ease-out;
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
  font-size: var(--fs-4xl);
  font-weight: var(--fw-bold);
  letter-spacing: var(--ls-tight);
  line-height: var(--lh-tight);
  margin-top: 12px;
}

.greet-desc {
  margin-top: 8px;
  color: var(--text-muted);
  font-size: var(--fs-sm);
}
.greet-desc b {
  color: var(--text-strong);
  font-feature-settings: 'tnum';
  font-weight: 700;
  margin: 0 2px;
}

.greet-right { display: flex; gap: 10px; flex-shrink: 0; }

/* ===== Stat row ===== */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
@media (max-width: 1080px) { .stat-row { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 540px)  { .stat-row { grid-template-columns: 1fr; } }

/* ===== Bento =====
   第一行:网关 ↔ 环境状态 顶部平齐 (1fr 1fr)
   「环境状态」卡片内部上下两块:env-grid + quick-actions
*/
.bento {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}
.bento-row-top {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: stretch;
}
@media (max-width: 1080px) {
  .bento-row-top { grid-template-columns: 1fr; }
}

.recent-list { min-height: 200px; }
.empty-host { padding: 24px 0; }

/* ===== Gateway table (扁平单行,与协议表一致) ===== */
.gw-table {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.gw-row {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--radius-md);
  background: var(--bg-elevated);
  border: 1px solid var(--hairline);
  transition: all var(--dur-base) var(--ease-glacis);
}
.gw-row:hover {
  background: var(--bg-deep);
  border-color: var(--input-border-hover);
  transform: translateX(2px);
}
.gw-cell { min-width: 0; display: flex; align-items: center; gap: 8px; flex-wrap: nowrap; }
.gw-cell-info {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex: 0 0 auto;
}
.gw-cell-tags {
  gap: 6px;
  flex-shrink: 0;
}
.gw-cell-actions {
  margin-left: auto;
  gap: 6px;
  flex-shrink: 0;
}

.gw-id {
  flex: 0 0 auto;
  min-width: 0;
  display: inline-flex;
  align-items: center;
}
.gw-name {
  font-weight: var(--fw-semibold);
  color: var(--text-strong);
  font-size: var(--fs-sm);
  letter-spacing: var(--ls-default);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
  flex: 0 1 auto;
  max-width: 220px;
}

/* 让 PageCard 在顶部对齐时,卡片标题区高度统一,
   借助 stretch 让网关卡 / 环境卡 顶底都平齐 */
.bento-row-top > * { display: flex; flex-direction: column; }
.bento-row-top > * > :first-child { flex: 0 0 auto; }
.bento-row-top > * > :last-child  { flex: 1 1 auto; }

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
  border-radius: var(--radius-lg);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline-soft);
  transition: all var(--dur-base) var(--ease-glacis);
}
.env-item:hover {
  background: var(--bg-elevated);
  border-color: var(--input-border-hover);
  transform: translateY(-1px);
}
.env-label {
  font-size: var(--fs-2xs);
  letter-spacing: var(--ls-wide);
  color: var(--text-faint);
  font-weight: var(--fw-medium);
}
.env-value {
  font-size: var(--fs-sm);
  color: var(--text-strong);
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-weight: var(--fw-medium);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--hairline);
}

/* ---- base card ---- */
.qa-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
  padding: 18px 10px 16px;
  border-radius: var(--radius-xl);
  background: var(--bg-elevated);
  border: 1px solid var(--hairline);
  cursor: pointer;
  overflow: hidden;
  transition:
    transform 280ms var(--ease-spring),
    box-shadow 280ms var(--ease),
    border-color 280ms var(--ease);
  font-family: inherit;
  text-align: center;
}

/* ---- diagonal shimmer overlay ---- */
.qa-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    transparent 40%,
    rgba(255, 255, 255, 0.06) 50%,
    transparent 60%
  );
  opacity: 0;
  transition: opacity 280ms var(--ease);
  pointer-events: none;
}
.qa-card:hover::before { opacity: 1; }

/* ---- hover lift + glow ---- */
.qa-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.10);
}

/* ---- badge ---- */
.qa-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: var(--fs-2xs);
  font-weight: var(--fw-bold);
  letter-spacing: var(--ls-wide);
  color: var(--text-faint);
  opacity: 0.5;
  transition: opacity 280ms var(--ease), color 280ms var(--ease);
}

/* ---- icon wrapper ---- */
.qa-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: var(--radius-lg);
  margin-bottom: 2px;
  border: 1px solid transparent;
  transition:
    background 280ms var(--ease),
    border-color 280ms var(--ease),
    transform 280ms var(--ease-spring);
}
.qa-icon .el-icon {
  font-size: 22px;
  transition: transform 280ms var(--ease-spring);
}

/* ---- label ---- */
.qa-label {
  font-size: var(--fs-sm);
  font-weight: var(--fw-semibold);
  color: var(--text-strong);
  letter-spacing: var(--ls-default);
  transition: color 280ms var(--ease);
}

/* ---- sub / subtitle ---- */
.qa-sub {
  font-size: var(--fs-2xs);
  color: var(--text-faint);
  letter-spacing: var(--ls-default);
  line-height: 1.5;
  margin-top: 2px;
  transition: color 280ms var(--ease);
}

/* ============================================================
   TEAL — 新建网关
   ============================================================ */
.qa-teal:hover { border-color: var(--primary-300); box-shadow: 0 8px 24px rgba(20, 184, 166, 0.14); }
.qa-teal:hover .qa-badge { opacity: 0.9; color: var(--primary-500); }
.qa-teal .qa-icon {
  background: rgba(20, 184, 166, 0.08);
  border-color: rgba(20, 184, 166, 0.18);
}
.qa-teal .qa-icon .el-icon { color: var(--primary-600); }
.qa-teal:hover .qa-icon {
  background: rgba(20, 184, 166, 0.14);
  border-color: rgba(20, 184, 166, 0.35);
  transform: scale(1.08);
}
.qa-teal:hover .qa-icon .el-icon { color: var(--primary-600); }
.qa-teal:hover .qa-label { color: var(--primary-700); }
.qa-teal:hover .qa-sub { color: var(--primary-500); }
:root.dark .qa-teal .qa-icon .el-icon { color: var(--primary-300); }
:root.dark .qa-teal:hover .qa-icon { background: rgba(20, 184, 166, 0.18); border-color: rgba(20, 184, 166, 0.40); }
:root.dark .qa-teal:hover .qa-icon .el-icon { color: var(--primary-300); }
:root.dark .qa-teal:hover .qa-label { color: var(--primary-200); }
:root.dark .qa-teal:hover .qa-sub { color: var(--primary-400); }

/* ============================================================
   VIOLET — 绑定工具
   ============================================================ */
.qa-violet:hover { border-color: rgba(139, 92, 246, 0.45); box-shadow: 0 8px 24px rgba(139, 92, 246, 0.14); }
.qa-violet:hover .qa-badge { opacity: 0.9; color: var(--violet-color); }
.qa-violet .qa-icon {
  background: rgba(139, 92, 246, 0.08);
  border-color: rgba(139, 92, 246, 0.18);
}
.qa-violet .qa-icon .el-icon { color: var(--violet-color); }
.qa-violet:hover .qa-icon {
  background: rgba(139, 92, 246, 0.14);
  border-color: rgba(139, 92, 246, 0.35);
  transform: scale(1.08);
}
.qa-violet:hover .qa-label { color: #7c3aed; }
.qa-violet:hover .qa-sub { color: #8b5cf6; }
:root.dark .qa-violet .qa-icon .el-icon { color: #a78bfa; }
:root.dark .qa-violet:hover .qa-icon { background: rgba(139, 92, 246, 0.18); border-color: rgba(139, 92, 246, 0.40); }
:root.dark .qa-violet:hover .qa-icon .el-icon { color: #a78bfa; }
:root.dark .qa-violet:hover .qa-label { color: #c4b5fd; }
:root.dark .qa-violet:hover .qa-sub { color: #a78bfa; }

/* ============================================================
   AMBER — 导入协议
   ============================================================ */
.qa-amber:hover { border-color: rgba(245, 158, 11, 0.45); box-shadow: 0 8px 24px rgba(245, 158, 11, 0.14); }
.qa-amber:hover .qa-badge { opacity: 0.9; color: var(--warn-color); }
.qa-amber .qa-icon {
  background: rgba(245, 158, 11, 0.08);
  border-color: rgba(245, 158, 11, 0.18);
}
.qa-amber .qa-icon .el-icon { color: var(--warn-color); }
.qa-amber:hover .qa-icon {
  background: rgba(245, 158, 11, 0.14);
  border-color: rgba(245, 158, 11, 0.35);
  transform: scale(1.08);
}
.qa-amber:hover .qa-label { color: #b45309; }
.qa-amber:hover .qa-sub { color: #f59e0b; }
:root.dark .qa-amber .qa-icon .el-icon { color: #fbbf24; }
:root.dark .qa-amber:hover .qa-icon { background: rgba(245, 158, 11, 0.18); border-color: rgba(245, 158, 11, 0.40); }
:root.dark .qa-amber:hover .qa-icon .el-icon { color: #fbbf24; }
:root.dark .qa-amber:hover .qa-label { color: #fcd34d; }
:root.dark .qa-amber:hover .qa-sub { color: #fbbf24; }

/* ============================================================
   EMERALD — 发放 API Key
   ============================================================ */
.qa-emerald:hover { border-color: rgba(16, 185, 129, 0.45); box-shadow: 0 8px 24px rgba(16, 185, 129, 0.14); }
.qa-emerald:hover .qa-badge { opacity: 0.9; color: var(--ok-color); }
.qa-emerald .qa-icon {
  background: rgba(16, 185, 129, 0.08);
  border-color: rgba(16, 185, 129, 0.18);
}
.qa-emerald .qa-icon .el-icon { color: var(--ok-color); }
.qa-emerald:hover .qa-icon {
  background: rgba(16, 185, 129, 0.14);
  border-color: rgba(16, 185, 129, 0.35);
  transform: scale(1.08);
}
.qa-emerald:hover .qa-label { color: #059669; }
.qa-emerald:hover .qa-sub { color: var(--ok-color); }
:root.dark .qa-emerald .qa-icon .el-icon { color: #34d399; }
:root.dark .qa-emerald:hover .qa-icon { background: rgba(16, 185, 129, 0.18); border-color: rgba(16, 185, 129, 0.40); }
:root.dark .qa-emerald:hover .qa-icon .el-icon { color: #34d399; }
:root.dark .qa-emerald:hover .qa-label { color: #6ee7b7; }
:root.dark .qa-emerald:hover .qa-sub { color: #34d399; }

/* ===== Bottom row:协议接入时间 + APIKey,各占一半(1:1) ===== */
.dashboard-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: stretch;
}
@media (max-width: 1080px) { .dashboard-row { grid-template-columns: 1fr; } }

/* ===== Protocol table (扁平大表) ===== */
.protocol-row {
  display: block;
}

.protocol-table {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* 表头 */
.pt-head {
  display: grid;
  grid-template-columns: 76px minmax(120px, 1.2fr) 70px minmax(0, 3fr);
  gap: 10px;
  align-items: center;
  padding: 0 10px 6px;
  border-bottom: 1px solid var(--hairline);
  color: var(--text-faint);
  font-size: var(--fs-2xs);
  letter-spacing: var(--ls-wider);
  text-transform: uppercase;
  font-weight: var(--fw-semibold);
}
@media (max-width: 960px) {
  .pt-head { display: none; }
}

/* 表体 */
.pt-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pt-row {
  display: grid;
  grid-template-columns: 76px minmax(120px, 1.2fr) 70px minmax(0, 3fr);
  gap: 10px;
  align-items: center;
  padding: 9px 10px;
  border-radius: var(--radius-md);
  background: var(--bg-elevated);
  border: 1px solid var(--hairline);
  transition: all var(--dur-base) var(--ease-glacis);
}
.pt-row:hover {
  background: var(--bg-deep);
  border-color: var(--input-border-hover);
  transform: translateX(2px);
}

.pt-col {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.pt-col-url { min-width: 0; }
.pt-col-url .url {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--fs-sm);
  color: var(--text-default);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.pt-col-headers {
  min-width: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.pt-col-headers .header-chips { flex: 1 1 auto; min-width: 0; }

.kv.kv-tail {
  flex: 0 0 auto;
  margin-left: auto;
  padding: 2px 8px;
  border-radius: var(--radius-md);
  background: var(--bg-deep);
  border: 1px solid var(--hairline);
  white-space: nowrap;
}
.kv.kv-tail .kv-k { font-size: var(--fs-2xs); }
.kv.kv-tail .kv-v { font-size: var(--fs-xs); color: var(--primary-700); }
:root.dark .kv.kv-tail .kv-v { color: var(--primary-300); }

@media (max-width: 960px) {
  .pt-row {
    grid-template-columns: 88px 1fr;
    grid-auto-rows: auto;
  }
  .pt-col-scheme,
  .pt-col-headers {
    grid-column: 1 / -1;
    padding-left: 100px;
  }
}


/* header / kv helpers */
.kv {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--fs-sm);
}
.kv-k {
  color: var(--text-faint);
  font-weight: var(--fw-medium);
}
.kv-v {
  color: var(--text-strong);
  font-weight: var(--fw-semibold);
  font-feature-settings: 'tnum';
}

.header-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  min-width: 0;
}
.header-chips-empty { opacity: 0.55; }

.header-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 9px;
  border-radius: var(--radius-pill, 999px);
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--fs-xs);
  line-height: 1.4;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-default);
  max-width: 100%;
  transition: all var(--dur-base) var(--ease-glacis);
}
.header-chip:hover {
  background: var(--bg-deep);
  border-color: var(--input-border-hover);
}

.header-chip-warn {
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.16), rgba(245, 158, 11, 0.06));
  border-color: rgba(245, 158, 11, 0.45);
  color: var(--amber-700, #b45309);
  box-shadow: 0 0 0 1px rgba(245, 158, 11, 0.18) inset;
}
:root.dark .header-chip-warn {
  color: #fbbf24;
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.22), rgba(245, 158, 11, 0.08));
}

.header-chip-empty {
  color: var(--text-faint);
  font-style: italic;
}

.header-k {
  font-weight: 700;
  color: var(--primary-600);
  letter-spacing: 0.01em;
}
:root.dark .header-k { color: var(--primary-300); }
.header-chip-warn .header-k {
  color: inherit;
  text-shadow: 0 0 1px currentColor;
}

.header-eq {
  color: var(--text-faint);
  font-weight: 500;
}

.header-v {
  color: var(--text-strong);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}
.header-chip-warn .header-v {
  font-feature-settings: 'tnum';
}

/* ===== APIKey table (扁平单行,与协议表一致)
   列:ID / Key / 频率 / 过期 / 操作
*/
.ak-table {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.ak-row {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1.2fr) auto auto auto;
  gap: 12px;
  align-items: center;
  padding: 8px 10px;
  border-radius: var(--radius-md);
  background: var(--bg-elevated);
  border: 1px solid var(--hairline);
  transition: all var(--dur-base) var(--ease-glacis);
}
.ak-row:hover {
  background: var(--bg-deep);
  border-color: var(--input-border-hover);
  transform: translateX(2px);
}
.ak-cell { min-width: 0; display: flex; align-items: center; gap: 6px; }
.ak-cell-info { min-width: 0; }
.ak-cell-key  { min-width: 0; }
.ak-cell-rate { justify-content: flex-end; }
.ak-cell-expire { justify-content: flex-end; }
.ak-cell-actions { justify-content: flex-end; gap: 6px; flex-shrink: 0; }

.ak-key {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--fs-sm);
  font-weight: var(--fw-semibold);
  color: var(--text-strong);
  letter-spacing: var(--ls-wide);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 220px;
  display: inline-block;
}
</style>
