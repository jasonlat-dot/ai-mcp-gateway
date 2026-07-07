import axios from 'axios'
import { ElMessage } from 'element-plus'

const AUTH_KEY = 'mcp_admin_token'
const USER_KEY = 'mcp_admin_user'
const IS_DEV   = import.meta.env.DEV

function ts() { return new Date().toISOString().split('T')[1].replace('Z', '') }

export function getToken() {
  return localStorage.getItem(AUTH_KEY)
}

export function setToken(token) {
  if (token) localStorage.setItem(AUTH_KEY, token)
  else localStorage.removeItem(AUTH_KEY)
}

export function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

export function setUser(user) {
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
  else localStorage.removeItem(USER_KEY)
}

const SUCCESS_CODES = new Set(['0000', 'SUCCESS_0000', '200'])

function createService() {
  const instance = axios.create({
    // 前端按 `/admin/...` 调用,经 Vite proxy rewrite 成 `/api-gateway/admin/...`
    baseURL: import.meta.env.VITE_API_BASE || '/admin',
    timeout: 30_000,
    headers: { 'Content-Type': 'application/json' },
  })

  instance.interceptors.request.use((config) => {
    const token = getToken()
    if (token) config.headers.Authorization = token

    if (IS_DEV) {
      // 拼成完整的 url,在浏览器 devtools 中一眼可见
      const fullUrl = (config.baseURL || '') + (config.url || '')
      // eslint-disable-next-line no-console
      console.log(
        `%c[axios → ${ts()}]`,
        'color:#3b82f6;font-weight:bold',
        config.method?.toUpperCase(),
        fullUrl,
        '\n  params:', config.params,
        '\n  body  :', config.data,
      )
    }
    return config
  })

  instance.interceptors.response.use(
    (response) => {
      if (IS_DEV) {
        const fullUrl = (response.config.baseURL || '') + (response.config.url || '')
        // eslint-disable-next-line no-console
        console.log(
          `%c[axios ← ${ts()}]`,
          'color:#10b981;font-weight:bold',
          response.status,
          response.config.method?.toUpperCase(),
          fullUrl,
          '\n  data  :', response.data,
        )
      }
      const payload = response.data
      if (payload && typeof payload === 'object' && 'code' in payload) {
        if (!SUCCESS_CODES.has(payload.code)) {
          ElMessage.error(payload.info || '请求失败')
          return Promise.reject(payload)
        }
      }
      return response
    },
    (error) => {
      if (error.response) {
        const { status, data } = error.response
        if (IS_DEV) {
          const fullUrl = (error.config?.baseURL || '') + (error.config?.url || '')
          // eslint-disable-next-line no-console
          console.error(
            `[axios ✗ ${ts()}]`,
            status,
            error.config?.method?.toUpperCase(),
            fullUrl,
            '\n  resp  :', data,
          )
        }
        if (status === 401 || status === 403) {
          setToken(null)
          setUser(null)
          if (window.location.hash !== '#/login') {
            window.location.hash = '#/login'
          }
          ElMessage.warning('登录状态已失效,请重新登录')
        } else {
          ElMessage.error(data?.info || `请求异常 ${status}`)
        }
      } else if (error.request) {
        if (IS_DEV) {
          // eslint-disable-next-line no-console
          console.error(`[axios ✗ ${ts()}] no response`, error.message)
        }
        ElMessage.error('网络不通,请检查后端服务是否启动')
      } else {
        if (IS_DEV) {
          // eslint-disable-next-line no-console
          console.error(`[axios ✗ ${ts()}]`, error.message)
        }
        ElMessage.error(error.message || '请求失败')
      }
      return Promise.reject(error)
    },
  )

  return instance
}

const service = createService()

/**
 * 统一解包:
 *  - 列表响应 → 直接是 array
 *  - 分页响应 → { list, total }
 *  - 单值/成功响应 → data 字段
 */
export function unpack(r) {
  const body = r.data
  if (!body) return body
  if (Array.isArray(body)) return body
  if ('total' in body || 'data' in body) {
    return {
      list: body.data ?? [],
      total: body.total ?? (Array.isArray(body.data) ? body.data.length : 0),
    }
  }
  return body
}

export default service
