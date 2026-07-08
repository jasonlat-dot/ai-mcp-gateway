/**
 * Vanguard · 全局时间格式化
 */
export function fmtDate(d) {
  if (!d) return '-'
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`
}

export function fmtDateOnly(d) {
  if (!d) return '-'
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())}`
}

export function localInputDate(d) {
  if (!d) return ''
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  const off = dt.getTimezoneOffset() * 60_000
  return new Date(dt.getTime() - off).toISOString().slice(0, 16)
}

/**
 * inputDateOrEmpty — 与 localInputDate 等价,
 *  名字更直白,语义是「后端原始值(可为 null)→ datetime-local 输入框值(空字符串)」。
 */
export function inputDateOrEmpty(d) {
  return localInputDate(d)
}

export function authBadge(auth) {
  if (auth === 1) return { label: '强校验', tone: 'success' }
  if (auth === 0) return { label: '不校验', tone: 'warning' }
  return { label: '未知', tone: 'default' }
}

export function statusBadge(status) {
  if (status === 1) return { label: '启用', tone: 'success' }
  if (status === 0) return { label: '禁用', tone: 'disabled' }
  return { label: '未知', tone: 'default' }
}

export function httpMethodBadge(method) {
  const map = {
    GET:     { tone: 'info',    label: 'GET' },
    POST:    { tone: 'success', label: 'POST' },
    PUT:     { tone: 'warning', label: 'PUT' },
    DELETE:  { tone: 'danger',  label: 'DEL' },
    PATCH:   { tone: 'warning', label: 'PATCH' },
  }
  return map[(method || '').toUpperCase()] || { tone: 'info', label: method || '-' }
}

/**
 * 解析后端 httpHeaders 字段
 *  - 支持 JSON 字符串 / 数组 / map / `k:v;k:v` / `k=v;k=v`
 *  - 失败时回退为单条 key
 *  - 永远返回 [{ k, v }] 数组
 */
export function parseHeaders(raw) {
  if (!raw) return []
  if (Array.isArray(raw)) {
    return raw.map((kv) => {
      if (typeof kv === 'string') {
        const i = kv.indexOf(':')
        return i > 0 ? { k: kv.slice(0, i).trim(), v: kv.slice(i + 1).trim() } : { k: kv, v: '' }
      }
      if (kv && typeof kv === 'object') {
        const [k, v] = Object.entries(kv)[0] || ['', '']
        return { k, v: String(v ?? '') }
      }
      return { k: String(kv), v: '' }
    })
  }
  if (typeof raw === 'object') {
    return Object.entries(raw).map(([k, v]) => ({ k, v: Array.isArray(v) ? v.join(',') : String(v ?? '') }))
  }
  if (typeof raw !== 'string') return []
  try {
    const obj = JSON.parse(raw)
    if (obj && typeof obj === 'object') {
      return Object.entries(obj).map(([k, v]) => ({ k, v: Array.isArray(v) ? v.join(',') : String(v ?? '') }))
    }
  } catch { /* not JSON */ }
  return raw.split(/[;\n]/).map((kv) => {
    const i = kv.indexOf(':')
    if (i < 0) return { k: kv.trim(), v: '' }
    return { k: kv.slice(0, i).trim(), v: kv.slice(i + 1).trim() }
  }).filter((x) => x.k)
}

/** 高亮敏感 header (key 包含 token / auth / key / secret / passwd / credential) */
export function isSensitiveHeader(k = '') {
  return /token|auth|key|secret|passwd|password|credential|cookie/i.test(String(k))
}
