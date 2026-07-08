import request, { unpack } from '@/utils/request'

/* ============================================================
 * Gateway · 协议  ->  /admin/protocol/**
 * ============================================================ */

export function pageGatewayProtocol(params) {
  return request.get('/protocol/query_gateway_protocol_page', { params }).then(unpack)
}

export function listGatewayProtocol() {
  return request.get('/protocol/query_gateway_protocol_list').then((r) => r.data.data || [])
}

export function listGatewayProtocolByGatewayId(gatewayId) {
  return request.get('/protocol/query_gateway_protocol_list_by_gateway_id', {
    params: { gatewayId },
  }).then((r) => r.data.data || [])
}

export function saveGatewayProtocol(payload) {
  return request.post('/protocol/save_gateway_protocol', payload)
}

export function updateGatewayProtocol(payload) {
  return request.post('/protocol/update_gateway_protocol', payload)
}

export function deleteGatewayProtocol(protocolId) {
  return request.post('/protocol/delete_gateway_protocol', null, {
    params: { protocolId },
  })
}

export function importGatewayProtocol(payload) {
  return request.post('/protocol/import_gateway_protocol', payload)
}

export function analysisProtocol(payload) {
  return request.post('/protocol/analysis_protocol', payload).then((r) => r.data.data || [])
}
