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

/* ============================================================
 * Gateway · Dubbo 协议  ->  /admin/dubbo-protocol/**
 *
 * 上传 dubbo-api-docs 生成的 Nacos metadata JSON 文件,
 * 后端解析出 mcp_protocol_dubbo + mcp_protocol_mapping 行。
 *
 * directUrl 由后端从 parameters.bind.ip + bind.port 自动拼出 dubbo://ip:port,
 * 前端预览时可编辑、确认导入。
 * ============================================================ */

/**
 * 解析 dubbo JSON(仅预览,不落库)。
 * @param {File} file 来自 <input type="file"> 的 File 对象
 * @returns Promise<{ protocols: DubboProtocolDTO[], bindIp: string|null, bindPort: string|null }>
 */
export function analysisDubboProtocol(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/dubbo-protocol/analysis_dubbo_protocol', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }).then((r) => r.data.data || { protocols: [], bindIp: null, bindPort: null })
}

/**
 * 解析并直接落库。
 * <p>
 * overrides 是用户在前端预览页编辑后的覆盖补丁,按 methodName 匹配回写到后端 VO。
 * @param {File} file 来自 <input type="file"> 的 File 对象
 * @param {Array<{methodName:string,directUrl?:string,directEnabled?:boolean}>} overrides
 */
export function importDubboProtocol(file, overrides) {
  const formData = new FormData()
  formData.append('file', file)
  if (overrides && overrides.length) {
    formData.append('overrides', JSON.stringify(overrides))
  }
  return request.post('/dubbo-protocol/import_dubbo_protocol', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/* ============================================================
 * Dubbo 协议列表 — 与 HTTP 协议列表平行,挂在 /admin/protocol/dubbo_*
 * 这里三个 endpoint 在 GatewayProtocolController 里实现,
 * 复用 HTTP 那个 Controller 的 prefix(/admin/protocol),不另开 controller。
 * ============================================================ */

export function listDubboProtocol() {
  return request.get('/protocol/query_dubbo_protocol_list').then((r) => r.data.data || [])
}

export function pageDubboProtocol(params) {
  return request.get('/protocol/query_dubbo_protocol_page', { params }).then(unpack)
}

export function listDubboProtocolByGatewayId(gatewayId) {
  return request.get('/protocol/query_dubbo_protocol_list_by_gateway_id', {
    params: { gatewayId },
  }).then((r) => r.data.data || [])
}

/**
 * 更新 Dubbo 协议配置。
 * <p>
 * payload 是 GatewayConfigRequestDTO.GatewayProtocol 形态,
 * 只填 dubboProtocols(httpProtocols 留空)即可。`update_gateway_protocol`
 * 接口会按 VO 是否带 protocolId 来路由到 update 逻辑。
 * @param {Array<{protocolId:number, groupName?:string, version?:string, methodName?:string,
 *                parameterTypes?:string[], timeout?:number, retryTimes?:number,
 *                directUrl?:string, directEnabled?:boolean, status?:number,
 *                mappings?:Array}>} dubboProtocols
 */
export function updateDubboProtocol(dubboProtocols) {
  return request.post('/protocol/update_gateway_protocol', {
    dubboProtocols,
  })
}
