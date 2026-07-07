import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '@/utils/request'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, layout: 'blank' },
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '控制台', icon: 'DataAnalysis' },
      },
      {
        path: 'gateways',
        name: 'gateways',
        component: () => import('@/views/gateway/GatewayListView.vue'),
        meta: { title: '网关列表', icon: 'Connection' },
      },
      {
        path: 'gateways/:id',
        name: 'gateways.detail',
        component: () => import('@/views/gateway/GatewayDetailView.vue'),
        meta: { title: '网关详情', hidden: true },
      },
      {
        path: 'tools',
        name: 'tools',
        component: () => import('@/views/gateway/GatewayToolView.vue'),
        meta: { title: '网关工具', icon: 'Tools' },
      },
      {
        path: 'protocols',
        name: 'protocols',
        component: () => import('@/views/gateway/GatewayProtocolView.vue'),
        meta: { title: '协议配置', icon: 'Share' },
      },
      {
        path: 'protocols/:id',
        name: 'protocols.detail',
        component: () => import('@/views/gateway/ProtocolDetailView.vue'),
        meta: { title: '协议详情', hidden: true },
      },
      {
        path: 'auth',
        name: 'auth',
        component: () => import('@/views/gateway/GatewayAuthView.vue'),
        meta: { title: '认证与限流', icon: 'Key' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'notfound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { public: true, layout: 'blank' },
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  if (to.meta?.public) return true
  if (!getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} · AI MCP Gateway`
  } else {
    document.title = 'AI MCP Gateway · Admin'
  }
})

export default router
