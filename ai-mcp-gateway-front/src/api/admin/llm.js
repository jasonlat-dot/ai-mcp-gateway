import request from '@/utils/request'

/** 通过 LLM 调用指定 MCP 网关。 */
export function testCallGateway(payload) {
  return request.post('/llm/test_call_gateway', payload, {
    timeout: Math.max(30_000, Number(payload.timeout || 0) + 10_000),
  }).then((response) => response.data || {})
}
