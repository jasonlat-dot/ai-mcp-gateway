import request, { unpack } from '@/utils/request'

/* ============================================================
 * Gateway · 工具  ->  /admin/tool/**
 * ============================================================ */

export function pageGatewayTool(params) {
  return request.get('/tool/query_gateway_tool_page', { params }).then(unpack)
}

export function listGatewayTool() {
  return request.get('/tool/query_gateway_tool_list').then((r) => r.data.data || [])
}

export function listGatewayToolByGatewayId(gatewayId) {
  return request.get('/tool/query_gateway_tool_list_by_gateway_id', {
    params: { gatewayId },
  }).then((r) => r.data.data || [])
}

export function saveGatewayTool(payload) {
  return request.post('/tool/save_gateway_tool_config', payload)
}

export function updateGatewayTool(payload) {
  return request.post('/tool/update_gateway_tool_config', payload)
}

export function deleteGatewayTool(gatewayId, toolId) {
  return request.post('/tool/delete_gateway_tool_config', null, {
    params: { gatewayId, toolId },
  })
}
