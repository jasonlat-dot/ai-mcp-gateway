<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const collapsed = ref(false)

const APP_MODE = (import.meta.env.MODE || 'dev').toString()
const API_BASE = (import.meta.env.VITE_API_BASE || '/admin').toString()

const navItems = [
  { path: '/dashboard',  title: '控制台',     icon: 'DataAnalysis',   desc: 'Gateway Insights' },
  { path: '/gateways',   title: '网关列表',   icon: 'Connection',     desc: 'Gateway Inventory' },
  { path: '/tools',      title: '网关工具',   icon: 'Tools',          desc: 'Bound Tools' },
  { path: '/protocols',  title: '协议配置',   icon: 'Share',          desc: 'HTTP Protocols' },
  { path: '/auth',       title: '认证限流',   icon: 'Key',            desc: 'API Key & Rate' },
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
  router.push({ path: '/login' })
}

const user = computed(() => auth.user || { nickname: 'Admin', username: 'admin' })
</script>

<template>
  <div class="admin-shell">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="brand">
        <div class="brand-mark">
          <svg viewBox="0 0 28 28" width="20" height="20">
            <defs>
              <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%"  stop-color="#2563eb"/>
                <stop offset="100%" stop-color="#0d9488"/>
              </linearGradient>
            </defs>
            <path d="M14 2 L26 8.5 V19.5 L14 26 L2 19.5 V8.5 Z"
                  fill="none" stroke="url(#bg)" stroke-width="1.6" />
            <circle cx="14" cy="14" r="3.4" fill="url(#bg)" />
          </svg>
        </div>
        <div v-if="!collapsed" class="brand-text">
          <div class="brand-title">Glacis</div>
          <div class="brand-sub">MCP Admin Console</div>
        </div>
      </div>

      <nav class="nav-list">
        <button
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: activePath === item.path }"
          @click="goto(item)"
        >
          <span class="nav-icon">
            <el-icon><component :is="item.icon" /></el-icon>
          </span>
          <span v-if="!collapsed" class="nav-meta">
            <span class="nav-title">{{ item.title }}</span>
            <span class="nav-desc">{{ item.desc }}</span>
          </span>
        </button>
      </nav>

      <div class="sidebar-foot">
        <button class="collapse-btn" @click="collapsed = !collapsed">
          <el-icon>
            <component :is="collapsed ? 'Expand' : 'Fold'" />
          </el-icon>
        </button>
      </div>
    </aside>

    <!-- 主体 -->
    <main class="admin-main" :class="{ 'is-collapsed': collapsed }">
      <header class="topbar">
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
          <el-tooltip content="刷新当前页" placement="bottom">
            <button class="icon-btn" @click="() => $router.go(0)">
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
            <button class="logout-btn" @click="handleLogout" title="退出登录">
              <el-icon><SwitchButton /></el-icon>
            </button>
          </div>
        </div>
      </header>

      <section class="page-host">
        <router-view v-slot="{ Component, route: r }">
          <transition name="route" mode="out-in" appear>
            <component :is="Component" :key="r.fullPath" />
          </transition>
        </router-view>
      </section>
    </main>
  </div>
</template>

<style scoped lang="scss">
.admin-shell {
  position: relative;
  display: grid;
  grid-template-columns: var(--sidebar-w) 1fr;
  min-height: 100dvh;
  background: var(--bg-base);
  transition: grid-template-columns var(--dur-slow) var(--ease);
}

/* ===== Sidebar ===== */
.sidebar {
  background: #ffffff;
  border-right: 1px solid var(--hairline);
  display: flex;
  flex-direction: column;
  padding: 18px 14px;
  position: sticky;
  top: 0;
  height: 100dvh;
  z-index: 4;
}

.sidebar.collapsed {
  padding-left: 14px;
  padding-right: 14px;
}

.admin-shell:has(.sidebar.collapsed) {
  grid-template-columns: var(--sidebar-w-collapsed) 1fr;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px 22px;
}

.brand-mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  background: var(--bg-sunken);
  border-radius: 9px;
  border: 1px solid var(--hairline);
}

.brand-text { line-height: 1.2; min-width: 0; }

.brand-title {
  color: var(--text-strong);
  font-weight: 700;
  font-size: 15px;
  letter-spacing: -0.01em;
}

.brand-sub {
  color: var(--text-faint);
  font-size: 10.5px;
  letter-spacing: 0.04em;
  margin-top: 2px;
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-muted);
  cursor: pointer;
  text-align: left;
  transition: all var(--dur-base) var(--ease);
}

.nav-item:hover {
  background: var(--bg-deep);
  color: var(--text-strong);
}

.nav-item .nav-icon {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
  color: var(--text-muted);
  font-size: 16px;
  transition: all var(--dur-base) var(--ease);
  flex-shrink: 0;
}

.nav-item.active {
  color: var(--accent);
  background: var(--accent-soft);
  border-color: var(--accent-line);
}

.nav-item.active .nav-icon {
  background: var(--accent);
  border-color: var(--accent);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}

.nav-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.nav-title {
  font-weight: 600;
  font-size: 13.5px;
  line-height: 1.1;
}

.nav-desc {
  font-size: 11px;
  letter-spacing: 0.01em;
  color: var(--text-faint);
  font-weight: 400;
}

.nav-item.active .nav-desc {
  color: var(--text-muted);
}

.sidebar-foot {
  margin-top: auto;
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid var(--hairline-soft);
}

.collapse-btn {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: transparent;
  border: 1px solid var(--hairline);
  color: var(--text-muted);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all var(--dur-base) var(--ease);
}

.collapse-btn:hover {
  background: var(--bg-deep);
  color: var(--text-strong);
  border-color: var(--hairline-strong);
}

/* ===== admin main ===== */
.admin-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 100dvh;
  position: relative;
  z-index: 1;
}

.topbar {
  position: sticky;
  top: 14px;
  z-index: 3;
  margin: 14px 16px 0;
  height: var(--topbar-h);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #ffffff;
  border: 1px solid var(--hairline);
  box-shadow: var(--shadow-xs);
}

.topbar-left, .topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.crumbs {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px;
  border-radius: 7px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
}

.crumb-text {
  color: var(--text-strong);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: -0.005em;
}

.env-tag {
  font-size: 11px;
  color: var(--text-muted);
  letter-spacing: 0.02em;
  font-family: 'JetBrains Mono', monospace;
  padding: 3px 9px;
  background: #ffffff;
  border: 1px solid var(--hairline);
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.env-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--teal);
  box-shadow: 0 0 6px rgba(13, 148, 136, 0.4);
}

.icon-btn {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid var(--hairline);
  color: var(--text-muted);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all var(--dur-base) var(--ease);
}

.icon-btn:hover {
  background: var(--bg-deep);
  color: var(--text-strong);
  border-color: var(--hairline-strong);
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 4px 4px 4px;
  border-radius: 999px;
  background: var(--bg-sunken);
  border: 1px solid var(--hairline);
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--accent);
  display: grid;
  place-items: center;
  color: #ffffff;
  font-weight: 700;
  font-size: 12.5px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
  padding-right: 6px;
}

.user-name {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--text-strong);
}

.user-role {
  font-size: 10.5px;
  color: var(--text-faint);
  letter-spacing: 0.02em;
  margin-top: 1px;
}

.logout-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-muted);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all var(--dur-base) var(--ease);
}

.logout-btn:hover {
  background: var(--rose-soft);
  color: var(--err-color);
  border-color: rgba(190, 24, 93, 0.22);
}

.page-host {
  flex: 1;
  padding: 22px 16px 28px;
  min-width: 0;
}

/* route transition */
.route-enter-active,
.route-leave-active {
  transition: opacity var(--dur-base) var(--ease), transform var(--dur-base) var(--ease);
}
.route-enter-from { opacity: 0; transform: translateY(4px); }
.route-leave-to   { opacity: 0; transform: translateY(-4px); }

@media (max-width: 1080px) {
  .admin-shell { grid-template-columns: var(--sidebar-w-collapsed) 1fr; }
  .sidebar { padding: 14px; }
  .brand-text, .nav-meta, .env-tag { display: none; }
  .nav-item { justify-content: center; padding: 10px 8px; }
}

@media (max-width: 720px) {
  .admin-shell { grid-template-columns: 1fr; }
  .sidebar { display: none; }
  .topbar { margin: 12px 12px 0; }
  .page-host { padding: 16px 12px 24px; }
}
</style>