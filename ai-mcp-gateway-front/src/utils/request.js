import axios from 'axios'

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

/**
 * 把任意 axios / 业务错误归一成 Error,message 由调用方直接拿来 toast。
 * 拦截器本身不再弹出任何 UI(避免与 ToastHost 双弹)。
 */
function normalizeError(reason) {
  // 业务错误:response.data 形如 { code, info }
  if (reason && typeof reason === 'object' && 'info' in reason) {
    const err = new Error(reason.info || '请求失败')
    err.code = reason.code
    err.payload = reason
    return err
  }
  if (reason && typeof reason === 'object' && 'message' in reason && reason instanceof Error) {
    return reason
  }
  if (reason instanceof Error) return reason
  return new Error('请求失败')
}

function normalizeHttpErrorPayload(status, data) {
  const body = data && typeof data === 'object' ? data : { data }
  return {
    ...body,
    code: body.code || String(status),
    info: body.info || body.message || (typeof data === 'string' && data.trim()) || `请求异常 ${status}`,
    httpStatus: status,
  }
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
          // 不再弹 UI,直接 reject,统一交给调用方处理。
          return Promise.reject(normalizeError(payload))
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
          // 401/403 仍然要主动提示用户,这是不可恢复的业务状态。
          return Promise.reject(normalizeError({
            ...normalizeHttpErrorPayload(status, data),
            info: '登录状态已失效,请重新登录',
          }))
        }
        return Promise.reject(normalizeError(normalizeHttpErrorPayload(status, data)))
      }
      if (error.request) {
        if (IS_DEV) {
          // eslint-disable-next-line no-console
          console.error(`[axios ✗ ${ts()}] no response`, error.message)
        }
        return Promise.reject(normalizeError({ info: '网络不通,请检查后端服务是否启动', code: 'NETWORK' }))
      }
      if (IS_DEV) {
        // eslint-disable-next-line no-console
        console.error(`[axios ✗ ${ts()}]`, error.message)
      }
      return Promise.reject(normalizeError(error))
    },
  )

  return instance
}

const service = createService()

/**
 * 统一解包:
 *  - 分页响应 (data 是数组, 顶层带 total) → { list, total }
 *  - 分页响应 (顶层有 data + total, data 可能是 array 也可能是对象) → { list, total }
 *  - 列表响应 (data 是 array, 顶层无 total) → 直接 array
 *  - 单值/成功响应 → 原 body
 *
 * 注: 后端 page 接口统一返 `{ code, info, data: [...], total: "N" }`,即 data 已经是
 *     当前页数据,total 是字符串数字。这里需要把它解构成 { list, total } 形式。
 */
function toNum(v) {
  if (v == null) return 0
  const n = typeof v === 'number' ? v : parseInt(String(v), 10)
  return Number.isFinite(n) ? n : 0
}

export function unpack(r) {
  const body = r && r.data
  if (!body || typeof body !== 'object') return body

  // 顶层就是分页对象 { data: [], total: "N" } —— 后端 page 接口的实际形态
  if ('data' in body && 'total' in body) {
    const list = Array.isArray(body.data) ? body.data : (body.data ? [body.data] : [])
    return { list, total: toNum(body.total) }
  }

  // 顶层就是分页对象 { list: [...], total: N } —— 历史/未来兼容
  if ('list' in body) {
    const list = Array.isArray(body.list) ? body.list : []
    return { list, total: toNum(body.total ?? list.length) }
  }

  // body 本身就是一个数组(无 total) → 当作 list 返回
  if (Array.isArray(body)) return body

  return body
}

export default service
