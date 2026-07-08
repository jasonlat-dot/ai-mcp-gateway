import request, { unpack } from '@/utils/request'

/* ============================================================
 * Gateway · 基础网关配置  ->  /admin/gateway/**
 * ============================================================ */

export function pageGatewayConfig(params) {
  return request.get('/gateway/query_gateway_config_page', { params }).then(unpack)
}

export function listGatewayConfig() {
  return request.get('/gateway/query_gateway_config_list').then((r) => r.data.data || [])
}

export function saveGatewayConfig(payload) {
  return request.post('/gateway/save_gateway_config', payload)
}

export function updateGatewayConfig(payload) {
  return request.post('/gateway/update_gateway_config', payload)
}

/**
 * 按 gatewayId 删除网关基础配置
 *  - 走 POST + params (与后端 deleteGatewayConfig 端点一致)
 *  - 后端 0 行(已删除) 视为幂等成功,success=true
 */
export function deleteGatewayConfig(gatewayId) {
  return request.post('/gateway/delete_gateway_config', null, {
    params: { gatewayId },
  })
}
