/* ============================================================
 * admin 共享工具方法
 *  - copyText: 剪贴板(浏览器安全上下文 fallback)
 *  - buildSseUrl: SSE 长连接绝对 URL(与 /admin base 无关)
 *  - buildStreamableUrl: Streamable HTTP 绝对 URL
 * ============================================================ */

export function copyText(text) {
  if (!text) return Promise.reject(new Error('empty'))
  if (navigator.clipboard && window.isSecureContext) {
    return navigator.clipboard.writeText(text).catch(() => fallback(text))
  }
  return fallback(text)
}

function fallback(text) {
  const ta = document.createElement('textarea')
  ta.value = text
  ta.style.position = 'fixed'
  ta.style.opacity = '0'
  document.body.appendChild(ta)
  ta.select()
  try {
    document.execCommand('copy')
  } finally {
    document.body.removeChild(ta)
  }
  return Promise.resolve()
}

export function buildSseUrl(gatewayId) {
  // SSE 必须用绝对地址(浏览器不会解析同源)。
  // - dev: 直接拼后端地址,绕过 Vite proxy(SSE 不易被 http-proxy 持有长连接)
  // - 生产: Nginx 已经反代过 /api-gateway,这里填空字符串即可
  const base = (import.meta.env.VITE_SSE_BASE || 'http://127.0.0.1:8888').replace(/\/$/, '')
  return `${base}/${gatewayId}/mcp/sse`
}

export function buildStreamableUrl(gatewayId) {
  const base = (import.meta.env.VITE_SSE_BASE || 'http://127.0.0.1:8888').replace(/\/$/, '')
  return `${base}/${gatewayId}/mcp`
}
