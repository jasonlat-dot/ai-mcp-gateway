import request, { unpack } from '@/utils/request'

/* ============================================================
 * Gateway · 认证  ->  /admin/auth/**
 * ============================================================ */

export function pageGatewayAuth(params) {
  return request.get('/auth/query_gateway_auth_page', { params }).then(unpack)
}

export function listGatewayAuth() {
  return request.get('/auth/query_gateway_auth_list').then((r) => r.data.data || [])
}

export function saveGatewayAuth(payload) {
  return request.post('/auth/save_gateway_auth', payload)
}

export function updateGatewayAuth(payload) {
  return request.post('/auth/update_gateway_auth', payload)
}

export function deleteGatewayAuth(gatewayId) {
  return request.post('/auth/delete_gateway_auth', null, {
    params: { gatewayId },
  })
}
