import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 后端基础地址:
//   1. 优先级 1: 命令行 VITE_API_BASE=http://127.0.0.1:9999/api-gateway npm run dev
//   2. 优先级 2: .env / .env.local 中的 VITE_API_BASE
//   3. 默认值    : http://127.0.0.1:8888/api-gateway
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')
  const BACKEND_BASE = env.VITE_API_BASE  || 'http://127.0.0.1:8888/api-gateway'
  const BACKEND_SSE  = env.VITE_SSE_BASE  || 'http://127.0.0.1:8888'
  const BACKEND_HOST = env.VITE_BACKEND_HOST || 'http://127.0.0.1:8888'

  // 启动时打印一份,方便确认 proxy 没走错
  // eslint-disable-next-line no-console
  console.log('\n[vite] backend proxy (BACKEND_BASE) =', BACKEND_BASE)
  // eslint-disable-next-line no-console
  console.log('[vite] backend SSE   (BACKEND_SSE)  =', BACKEND_SSE, '\n')

  /**
   * 在终端打印每一次代理转发的请求和响应,用于排查 404
   */
  function logProxy(label, color) {
    return (req, _res) => {
      // eslint-disable-next-line no-console
      console.log(
        `  \x1b[${color}m[proxy:${label}]\x1b[0m`,
        req.method,
        req.url,
      )
    }
  }

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern-compiler',
          additionalData: `@use "@/styles/vars.scss" as *;`,
        },
      },
    },
    server: {
      port: 5173,
      open: true,
      host: '127.0.0.1',
      proxy: {
        // 前端 axios 的 baseURL = '/admin'
        // 浏览器实际请求:  /admin/query_gateway_tool_page
        // proxy rewrite:    /admin/xxx  ->  /api-gateway/admin/xxx
        // 最终打到后端:     http://127.0.0.1:8888/api-gateway/admin/query_gateway_tool_page
        //
        // 注意:target 必须是 host,不能带 path(http-proxy 会把 target.path 拼到前面),
        // 否则会变成 /api-gateway/api-gateway/admin/... 双 prefix。
        '/admin': {
          target: BACKEND_HOST,
          changeOrigin: true,
          ws: true,
          rewrite: (path) => path.replace(/^\/admin/, '/api-gateway/admin'),
          configure: (proxy) => {
            proxy.on('proxyReq',  logProxy('admin -> backend',  '36'))
            proxy.on('proxyRes', (proxyRes) => {
              // eslint-disable-next-line no-console
              console.log(
                `  \x1b[35m[proxy:admin  resp]\x1b[0m`,
                proxyRes.statusCode,
                proxyRes.req?.method,
                proxyRes.req?.path,
              )
            })
            proxy.on('error', (err, _req, res) => {
              // eslint-disable-next-line no-console
              console.error('[proxy:admin  ERROR]', err.message)
              try { res && res.end && res.end() } catch {}
            })
          },
        },
        // 直透:有些页面可能直接调 /api-gateway/...
        '/api-gateway': {
          target: BACKEND_HOST,
          changeOrigin: true,
          ws: true,
          configure: (proxy) => {
            proxy.on('proxyReq',  logProxy('api-gateway -> backend', '33'))
            proxy.on('proxyRes', (proxyRes) => {
              // eslint-disable-next-line no-console
              console.log(
                `  \x1b[33m[proxy:api-gw resp]\x1b[0m`,
                proxyRes.statusCode,
                proxyRes.req?.method,
                proxyRes.req?.path,
              )
            })
            proxy.on('error', (err) => {
              // eslint-disable-next-line no-console
              console.error('[proxy:api-gw  ERROR]', err.message)
            })
          },
        },
        // SSE
        '/mcp': {
          target: BACKEND_SSE,
          changeOrigin: true,
          ws: true,
          configure: (proxy) => {
            proxy.on('proxyReq',  logProxy('mcp -> backend', '34'))
            proxy.on('error', (err) => {
              // eslint-disable-next-line no-console
              console.error('[proxy:mcp  ERROR]', err.message)
            })
          },
        },
      },
    },
    build: {
      target: 'es2020',
      sourcemap: false,
      chunkSizeWarningLimit: 1500,
    },
  }
})
