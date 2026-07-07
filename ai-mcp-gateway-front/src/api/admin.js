import request, { unpack } from '@/utils/request'

// 这里不能再写 /admin 了 —— request.js 已经把 baseURL 设成 '/admin'
// 这里每个接口拼的只是接口的方法名

/* ============================================================
 * Gateway · 基础网关配置
 * ============================================================ */

export function pageGatewayConfig(params) {
  return request.get('/query_gateway_config_page', { params }).then(unpack)
}

export function listGatewayConfig() {
  return request.get('/query_gateway_config_list').then((r) => r.data.data || [])
}

export function saveGatewayConfig(payload) {
  return request.post('/save_gateway_config', payload)
}

export function updateGatewayConfig(payload) {
  return request.post('/update_gateway_config', payload)
}

/* ============================================================
 * Gateway · 工具
 * ============================================================ */

export function pageGatewayTool(params) {
  return request.get('/query_gateway_tool_page', { params }).then(unpack)
}

export function listGatewayTool() {
  return request.get('/query_gateway_tool_list').then((r) => r.data.data || [])
}

export function listGatewayToolByGatewayId(gatewayId) {
  return request.get('/query_gateway_tool_list_by_gateway_id', {
    params: { gatewayId },
  }).then((r) => r.data.data || [])
}

export function saveGatewayTool(payload) {
  return request.post('/save_gateway_tool_config', payload)
}

export function updateGatewayTool(payload) {
  return request.post('/update_gateway_tool_config', payload)
}

export function deleteGatewayTool(gatewayId, toolId) {
  return request.post('/delete_gateway_tool_config', null, {
    params: { gatewayId, toolId },
  })
}

/* ============================================================
 * Gateway · 协议
 * ============================================================ */

export function pageGatewayProtocol(params) {
  return request.get('/query_gateway_protocol_page', { params }).then(unpack)
}

export function listGatewayProtocol() {
  return request.get('/query_gateway_protocol_list').then((r) => r.data.data || [])
}

export function listGatewayProtocolByGatewayId(gatewayId) {
  return request.get('/query_gateway_protocol_list_by_gateway_id', {
    params: { gatewayId },
  }).then((r) => r.data.data || [])
}

export function saveGatewayProtocol(payload) {
  return request.post('/save_gateway_protocol', payload)
}

export function updateGatewayProtocol(payload) {
  return request.post('/update_gateway_protocol', payload)
}

export function deleteGatewayProtocol(protocolId) {
  return request.post('/delete_gateway_protocol', null, {
    params: { protocolId },
  })
}

export function importGatewayProtocol(payload) {
  return request.post('/import_gateway_protocol', payload)
}

export function analysisProtocol(payload) {
  return request.post('/analysis_protocol', payload).then((r) => r.data.data || [])
}

/* ============================================================
 * Gateway · 认证
 * ============================================================ */

export function pageGatewayAuth(params) {
  return request.get('/query_gateway_auth_page', { params }).then(unpack)
}

export function listGatewayAuth() {
  return request.get('/query_gateway_auth_list').then((r) => r.data.data || [])
}

export function saveGatewayAuth(payload) {
  return request.post('/save_gateway_auth', payload)
}

export function updateGatewayAuth(payload) {
  return request.post('/update_gateway_auth', payload)
}

export function deleteGatewayAuth(gatewayId) {
  return request.post('/delete_gateway_auth', null, {
    params: { gatewayId },
  })
}

/* ============================================================
 * 业务辅助
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