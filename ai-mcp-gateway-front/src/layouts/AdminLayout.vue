<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import { useToast } from '@/composables/useToast'
import {
  DataAnalysis, Connection, Tools, Share, Key, Refresh, SwitchButton,
  Expand, Fold, Sunny, Moon, Search, Location,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToast()

const collapsed = ref(false)

const APP_MODE = (import.meta.env.MODE || 'dev').toString()
const API_BASE = (import.meta.env.VITE_API_BASE || '/admin').toString()

const navItems = [
  { path: '/dashboard',  title: '控制台',   icon: 'DataAnalysis', desc: 'Gateway Insights' },
  { path: '/gateways',   title: '网关列表', icon: 'Connection',   desc: 'Gateway Inventory' },
  { path: '/tools',      title: '网关工具', icon: 'Tools',        desc: 'Bound Tools' },
  { path: '/protocols',  title: 'HTTP 协议', icon: 'Share',      desc: 'OpenAPI Protocols' },
  { path: '/dubbo-protocols', title: 'Dubbo 协议', icon: 'Connection', desc: 'dubbo-api-docs' },
  { path: '/auth',       title: '认证限流', icon: 'Key',          desc: 'API Key & Rate' },
  { path: '/llm-test',   title: 'LLM 网关联调', icon: 'ChatDotRound', desc: 'Gateway Playground' },
]

const activePath = computed(() => {
  const p = route.path
  const match = navItems.find((n) => p === n.path || p.startsWith(n.path + '/'))
  return match?.path ?? '/dashboard'
})

function goto(item) {
  if (route.path !== item.path) router.push(item.path)
}

function handleLogout() {
  auth.logout()
  toast.info('已退出登录', { duration: 1800 })
  router.push({ path: '/login' })
}

function handleRefresh() {
  router.go(0)
}

const user = computed(() => auth.user || { nickname: 'Admin', username: 'admin' })

// Esc collapses sidebar
function onKey(e) { if (e.key === 'Escape' && !collapsed.value) collapsed.value = true }
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <div class="admin-shell" :class="{ 'is-collapsed': collapsed }">
    <!-- 侧边栏 -->
    <aside class="sidebar admin-sidebar" :class="{ collapsed }">
      <div class="brand">
        <div class="brand-mark">
          <svg viewBox="0 0 28 28" width="22" height="22" aria-hidden="true">
            <defs>
              <linearGradient id="brandGrad" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%"  stop-color="#14b8a6"/>
                <stop offset="100%" stop-color="#0d9488"/>
              </linearGradient>
            </defs>
            <path d="M14 2 L26 8.5 V19.5 L14 26 L2 19.5 V8.5 Z"
                  fill="none" stroke="url(#brandGrad)" stroke-width="1.6" />
            <circle cx="14" cy="14" r="3.4" fill="url(#brandGrad)" />
          </svg>
        </div>
        <div v-if="!collapsed" class="brand-text">
          <div class="brand-title">MCP Gateway</div>
          <div class="brand-sub">AI MCP Gateway Console</div>
        </div>
      </div>

      <nav class="sidebar-nav nav-list">
        <div class="sidebar-section">
          <div v-if="!collapsed" class="sidebar-section-title">控制台</div>
          <button
            v-for="item in navItems"
            :key="item.path"
            class="sidebar-link nav-item"
            :class="{ 'sidebar-link-active active': activePath === item.path }"
            @click="goto(item)"
          >
            <span class="icon">
              <el-icon><component :is="item.icon" /></el-icon>
            </span>
            <span v-if="!collapsed" class="nav-meta">
              <span class="nav-title">{{ item.title }}</span>
              <span class="nav-desc">{{ item.desc }}</span>
            </span>
          </button>
        </div>
      </nav>

      <div class="sidebar-footer sidebar-foot">
        <button class="btn btn-secondary btn-sm collapse-btn" @click="collapsed = !collapsed">
          <el-icon><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
          <span v-if="!collapsed">收起</span>
        </button>
      </div>
    </aside>

    <!-- 主体 -->
    <main class="admin-main" :class="{ 'is-collapsed': collapsed }">
      <header class="topbar admin-topbar">
        <div class="topbar-left">
          <div class="crumbs">
            <span class="crumb-text">{{ route.meta?.title || '控制台' }}</span>
          </div>
          <span class="env-tag">
            <span class="env-dot" />
            {{ APP_MODE }} · {{ API_BASE }}
          </span>
        </div>
        <div class="topbar-right">
          <ThemeToggle />
          <el-tooltip placement="bottom" :raw-content="true" :show-after="100">
            <template #content>刷新当前页</template>
            <button class="icon-btn btn btn-ghost btn-icon" @click="handleRefresh" aria-label="刷新当前页">
              <el-icon><Refresh /></el-icon>
            </button>
          </el-tooltip>
          <div class="user-pill">
            <div class="avatar">
              {{ (user.nickname || user.username || 'A').slice(0, 1).toUpperCase() }}
            </div>
            <div class="user-meta">
              <div class="user-name">{{ user.nickname || user.username }}</div>
              <div class="user-role">{{ user.role || 'Administrator' }}</div>
            </div>
            <el-tooltip placement="bottom" :raw-content="true" :show-after="100">
              <template #content>退出登录</template>
              <button class="logout-btn btn btn-ghost btn-icon btn-sm" @click="handleLogout" aria-label="退出登录">
                <el-icon><SwitchButton /></el-icon>
              </button>
            </el-tooltip>
          </div>
        </div>
      </header>

      <section class="page-host">
        <router-view v-slot="{ Component, route: r }">
          <component :is="Component" v-if="Component" :key="r.fullPath" />
        </router-view>
      </section>
    </main>
  </div>
</template>

<style scoped>
/* ============================================================
 * AdminLayout — MCP Gateway Admin (Sub2API-inspired)
 * Sidebar (sub2api .sidebar class) + Teal active indicator
 * Topbar: floating glass card, ThemeToggle, env tag, user pill
 * ============================================================ */
.admin-shell {
  position: relative;
  display: grid;
  grid-template-columns: var(--sidebar-w) 1fr;
  min-height: 100dvh;
  background: var(--bg-base);
  transition: grid-template-columns var(--dur-slow) var(--ease-glacis);
}
.admin-shell.is-collapsed { grid-template-columns: var(--sidebar-w-collapsed) 1fr; }

/* ===== Sidebar ===== */
.admin-sidebar {
  background: var(--bg-elevated);
  border-right: 1px solid var(--hairline);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100dvh;
  z-index: 4;
  padding: 0;
  width: var(--sidebar-w);
  border-radius: 0;
  border: 0;
  border-right: 1px solid var(--hairline);
  box-shadow: none;
  backdrop-filter: none;
}
.admin-sidebar.collapsed { width: var(--sidebar-w-collapsed); }

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 22px;
  height: var(--topbar-h);
  border-bottom: 1px solid var(--hairline);
  flex-shrink: 0;
}
.admin-sidebar.collapsed .brand { padding: 0; justify-content: center; }

.brand-mark {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  background: var(--info-soft);
  border-radius: var(--radius-lg);
  border: 1px solid var(--info-line);
  transition: transform var(--dur-base) var(--ease-glacis);
}
.brand-mark:hover { transform: rotate(-8deg) scale(1.04); }

.brand-text { line-height: 1.2; min-width: 0; }
.brand-title {
  color: var(--text-strong);
  font-weight: var(--fw-bold);
  font-size: var(--fs-base);
  letter-spacing: var(--ls-snug);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.brand-sub {
  color: var(--text-faint);
  font-size: var(--fs-3xs);
  letter-spacing: var(--ls-wide);
  margin-top: 2px;
}

.nav-list {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.sidebar-section { margin-bottom: 16px; }

.nav-item {
  position: relative;
  margin: 2px 0;
  background: transparent;
  border: 0;
  text-align: left;
  width: 100%;
  padding: 10px 14px;
  font-family: inherit;
}
.nav-item .icon {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-md);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-muted);
  font-size: var(--fs-xl);
  flex-shrink: 0;
  transition: all var(--dur-base) var(--ease-glacis);
}
.nav-item:hover .icon {
  background: var(--bg-deep);
  color: var(--text-strong);
}
.nav-item.active .icon {
  background: var(--gradient-primary);
  border-color: transparent;
  color: #ffffff;
  box-shadow: var(--shadow-glow);
}

.nav-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  margin-left: 12px;
}
.nav-title { font-weight: var(--fw-semibold); font-size: var(--fs-sm); line-height: 1.1; }
.nav-desc  { font-size: var(--fs-2xs); letter-spacing: var(--ls-wide); color: var(--text-faint); }
.nav-item.active .nav-desc { color: var(--primary-600); opacity: 0.7; }
:root.dark .nav-item.active .nav-desc { color: var(--primary-300); }

.sidebar-foot {
  padding: 14px;
  border-top: 1px solid var(--hairline);
  flex-shrink: 0;
}
.admin-sidebar.collapsed .sidebar-foot { display: flex; justify-content: center; padding: 14px 8px; }
.collapse-btn { width: 100%; }
.admin-sidebar.collapsed .collapse-btn { width: 40px; padding: 0; }

/* ===== admin main ===== */
.admin-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 100dvh;
  position: relative;
  z-index: 1;
}

.admin-topbar {
  position: sticky;
  top: 14px;
  z-index: 3;
  margin: 14px 16px 0;
  height: var(--topbar-h);
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: var(--bg-glass);
  backdrop-filter: blur(var(--blur-lg));
  -webkit-backdrop-filter: blur(var(--blur-lg));
  border: 1px solid var(--hairline);
  box-shadow: var(--shadow-card);
  transition: all var(--dur-slow) var(--ease-glacis);
}

.topbar-left, .topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.crumbs {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px;
  border-radius: var(--radius-md);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
}
.crumb-text {
  color: var(--text-strong);
  font-size: var(--fs-sm);
  font-weight: var(--fw-semibold);
  letter-spacing: var(--ls-default);
}

.env-tag {
  font-size: var(--fs-2xs);
  color: var(--text-muted);
  letter-spacing: var(--ls-wide);
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  padding: 3px 10px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-pill);
  display: flex;
  align-items: center;
  gap: 6px;
}
.env-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary-500);
  box-shadow: 0 0 8px rgba(20, 184, 166, 0.5);
  animation: pulseSoft 2.4s ease-in-out infinite;
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px;
  border-radius: var(--radius-pill);
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  transition: all var(--dur-base) var(--ease-glacis);
}
.user-pill:hover {
  border-color: var(--input-border-hover);
  background: var(--bg-deep);
}
.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: grid;
  place-items: center;
  color: #ffffff;
  font-weight: var(--fw-bold);
  font-size: var(--fs-xs);
  letter-spacing: var(--ls-snug);
  box-shadow: var(--shadow-glow);
}
.user-meta { display: flex; flex-direction: column; line-height: 1.15; padding: 0 4px; }
.user-name { font-size: var(--fs-xs); font-weight: var(--fw-semibold); color: var(--text-strong); letter-spacing: var(--ls-snug); }
.user-role { font-size: var(--fs-3xs); color: var(--text-faint); letter-spacing: var(--ls-wide); margin-top: 1px; }

.logout-btn { color: var(--text-muted); }
.logout-btn:hover {
  background: var(--err-soft) !important;
  color: var(--err-color) !important;
}

.page-host {
  position: relative;
  display: block;
  flex: 1 1 auto;
  padding: 22px 16px 28px;
  min-width: 0;
  min-height: calc(100dvh - var(--topbar-h) - 60px);
  animation: fadeIn 0.3s ease-out;
}

/* route transition rules are defined globally in src/styles/global.css (.route-*) */

@media (max-width: 1080px) {
  .admin-shell { grid-template-columns: var(--sidebar-w-collapsed) 1fr; }
  .admin-sidebar { padding: 0; }
  .brand-text, .nav-meta, .env-tag { display: none; }
  .nav-item { justify-content: center; padding: 10px 8px; }
}
@media (max-width: 720px) {
  .admin-shell { grid-template-columns: 1fr; }
  .admin-sidebar { display: none; }
  .admin-topbar { margin: 12px 12px 0; }
  .page-host { padding: 16px 12px 24px; }
}
</style>
