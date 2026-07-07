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

export function authBadge(auth) {
  if (auth === 1) return { label: '启用', tone: 'success' }
  if (auth === 0) return { label: '禁用', tone: 'info' }
  return { label: '未知', tone: 'info' }
}

export function statusBadge(status) {
  if (status === 1) return { label: '强校验', tone: 'primary' }
  if (status === 0) return { label: '不校验', tone: 'warning' }
  return { label: '未知', tone: 'info' }
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
