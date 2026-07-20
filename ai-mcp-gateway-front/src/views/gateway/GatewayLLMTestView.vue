<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageCard from '@/components/ui/PageCard.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import { listGatewayAuth, listGatewayConfig, testCallGateway } from '@/api/admin'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const toast = useToast()
const gateways = ref([])
const auths = ref([])
const loadingOptions = ref(false)
const submitting = ref(false)
const result = ref(null)
const elapsed = ref(null)

const form = reactive({
  gatewayId: '',
  authApiKey: '',
  mcpType: 'sse',
  timeout: 30_000,
  message: '',
})

const currentGateway = computed(() => gateways.value.find((item) => item.gatewayId === form.gatewayId))
const gatewayAuths = computed(() => auths.value.filter((item) => item.gatewayId === form.gatewayId))
const requiresAuth = computed(() => Number(currentGateway.value?.auth) === 1)
const strictKeyValidation = computed(() => Number(currentGateway.value?.status) === 1)
const canSubmit = computed(() => {
  if (!form.gatewayId || !form.message.trim() || submitting.value) return false
  return !requiresAuth.value || Boolean(form.authApiKey)
})

function maskKey(key) {
  if (!key) return '未配置 Key'
  if (key.length <= 14) return key
  return `${key.slice(0, 8)}••••${key.slice(-6)}`
}

function formatResultContent(value) {
  if (typeof value === 'string') return value || '（响应内容为空）'
  if (value == null) return '（响应内容为空）'
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}

watch(() => form.gatewayId, () => {
  if (!gatewayAuths.value.some((item) => item.apiKey === form.authApiKey)) {
    form.authApiKey = gatewayAuths.value[0]?.apiKey || ''
  }
  result.value = null
  elapsed.value = null
})

async function loadOptions() {
  loadingOptions.value = true
  try {
    const [gatewayRows, authRows] = await Promise.all([listGatewayConfig(), listGatewayAuth()])
    gateways.value = gatewayRows || []
    auths.value = authRows || []
    const preset = typeof route.query.gatewayId === 'string' ? route.query.gatewayId : ''
    form.gatewayId = gateways.value.some((item) => item.gatewayId === preset)
      ? preset
      : (gateways.value[0]?.gatewayId || '')
  } catch (error) {
    toast.error(error.message || '加载网关配置失败')
  } finally {
    loadingOptions.value = false
  }
}

async function submit() {
  if (!canSubmit.value) return
  submitting.value = true
  result.value = null
  elapsed.value = null
  const startedAt = performance.now()
  const transport = form.mcpType
  try {
    const response = await testCallGateway({
      gatewayId: form.gatewayId,
      authApiKey: form.authApiKey || null,
      mcpType: transport,
      timeout: Number(form.timeout),
      message: form.message.trim(),
    })
    const data = response.data
    result.value = {
      status: 'success',
      code: response.code || 'SUCCESS',
      info: response.info || '调用成功',
      transport,
      content: formatResultContent(data?.content ?? data),
    }
    elapsed.value = Math.round(performance.now() - startedAt)
    toast.success('网关调用成功', { duration: 1800 })
  } catch (error) {
    elapsed.value = Math.round(performance.now() - startedAt)
    const payload = error?.payload
    const message = payload?.info || error?.message || '网关调用失败'
    result.value = {
      status: 'error',
      code: payload?.code || error?.code || 'REQUEST_FAILED',
      info: message,
      transport,
      content: formatResultContent(payload || { error: message }),
    }
    toast.error(message)
  } finally {
    submitting.value = false
  }
}

onMounted(loadOptions)
</script>

<template>
  <div class="llm-page">
    <header class="page-heading">
      <div>
        <span class="eyebrow">Gateway Playground</span>
        <h1>LLM 网关联调</h1>
        <p>选择 MCP 传输协议、网关及认证 Key，发送自然语言请求验证工具调用链路。</p>
      </div>
      <StatusPill :tone="submitting ? 'warning' : 'info'">{{ submitting ? '调用中' : '准备就绪' }}</StatusPill>
    </header>

    <div class="workspace">
      <PageCard eyebrow="Request" title="调用参数" desc="认证 Key 会跟随所选网关自动切换">
        <div v-loading="loadingOptions" class="form-grid">
          <label class="field">
            <span>网关</span>
            <el-select v-model="form.gatewayId" class="selected-value-highlight" filterable placeholder="请选择网关">
              <el-option
                v-for="gateway in gateways"
                :key="gateway.gatewayId"
                :label="`${gateway.gatewayName || gateway.gatewayId} · ${gateway.gatewayId}`"
                :value="gateway.gatewayId"
              />
            </el-select>
          </label>

          <label class="field">
            <span>Auth API Key <em v-if="requiresAuth">必填</em></span>
            <el-select
              v-model="form.authApiKey"
              class="selected-value-highlight"
              clearable
              filterable
              :disabled="!form.gatewayId || gatewayAuths.length === 0"
              :placeholder="gatewayAuths.length ? '请选择认证 Key' : '该网关暂无认证 Key'"
            >
              <el-option
                v-for="(auth, index) in gatewayAuths"
                :key="`${auth.gatewayId}-${auth.apiKey}`"
                :label="`Key ${index + 1} · ${maskKey(auth.apiKey)}`"
                :value="auth.apiKey"
              />
            </el-select>
            <small v-if="gatewayAuths.length > 1">当前网关配置了 {{ gatewayAuths.length }} 个 Key，可任选其一测试。</small>
          </label>

          <div v-if="currentGateway" class="auth-notice" :class="strictKeyValidation ? 'is-strict' : 'is-relaxed'">
            <el-icon><Lock v-if="strictKeyValidation" /><Unlock v-else /></el-icon>
            <div>
              <strong>{{ strictKeyValidation ? 'API Key 强校验已开启' : 'API Key 强校验未开启' }}</strong>
              <span v-if="strictKeyValidation && requiresAuth">请求必须携带当前网关下有效且未过期的 Auth API Key。</span>
              <span v-else-if="requiresAuth">网关已启用认证，建议选择有效 Key；当前未启用强校验。</span>
              <span v-else>当前网关未启用认证，本次测试可以不选择 Auth API Key。</span>
            </div>
          </div>

          <label class="field">
            <span>超时时间</span>
            <el-input-number v-model="form.timeout" :min="1000" :max="300000" :step="1000" controls-position="right" />
            <small>单位：毫秒</small>
          </label>

          <label class="field">
            <span>MCP 传输协议</span>
            <el-radio-group v-model="form.mcpType" class="transport-options" :disabled="submitting">
              <el-radio-button value="sse">SSE</el-radio-button>
              <el-radio-button value="streamable">Streamable HTTP</el-radio-button>
            </el-radio-group>
            <small>{{ form.mcpType === 'streamable' ? '请求地址：/{gatewayId}/mcp' : '请求地址：/{gatewayId}/mcp/sse' }}</small>
          </label>

          <label class="field message-field">
            <span>请求内容</span>
            <el-input
              v-model="form.message"
              type="textarea"
              :rows="9"
              resize="vertical"
              placeholder="例如：查询北京地区的员工信息，并整理为表格。"
              @keydown.ctrl.enter.prevent="submit"
              @keydown.meta.enter.prevent="submit"
            />
            <small>Ctrl / ⌘ + Enter 快速发送</small>
          </label>

          <button class="btn btn-primary submit-btn" :disabled="!canSubmit" @click="submit">
            <el-icon v-if="!submitting"><Promotion /></el-icon>
            <span v-else class="spinner" />
            {{ submitting ? '正在调用网关…' : '发送测试请求' }}
          </button>
        </div>
      </PageCard>

      <PageCard eyebrow="Response" title="LLM 响应" desc="成功与失败结果都会在这里完整展示">
        <div
          class="response"
          :class="{ empty: !result && !submitting, 'is-loading': submitting }"
          :aria-busy="submitting"
        >
          <div v-if="submitting" class="response-loading" role="status" aria-live="polite">
            <div class="loading-meta">
              <span class="loading-state"><span class="loading-dot" /> 调用中</span>
              <span>
                {{ form.mcpType === 'streamable' ? 'Streamable HTTP' : 'SSE' }}
                · 配置超时 {{ Math.round(Number(form.timeout) / 1000) }} s
              </span>
            </div>

            <div class="loading-body">
              <div class="loading-wave" aria-hidden="true">
                <span /><span /><span /><span /><span />
              </div>
              <strong>正在等待 LLM 响应</strong>
              <span>网关正在建立 MCP 会话并执行工具调用，返回结果或超时后会自动结束。</span>
            </div>

            <div class="loading-skeleton" aria-hidden="true">
              <span class="skeleton-line line-short" />
              <span class="skeleton-line line-long" />
              <span class="skeleton-line line-medium" />
              <span class="skeleton-line line-long" />
            </div>
          </div>

          <div v-else-if="result" class="response-meta">
            <div class="response-status">
              <StatusPill :tone="result.status === 'success' ? 'success' : 'danger'">
                {{ result.status === 'success' ? '调用成功' : '调用失败' }}
              </StatusPill>
              <span class="response-code">{{ result.code }}</span>
            </div>
            <span>{{ elapsed }} ms · {{ result.transport === 'streamable' ? 'Streamable HTTP' : 'SSE' }}</span>
          </div>
          <div v-if="result && !submitting" class="response-info" :class="`is-${result.status}`">{{ result.info }}</div>
          <pre v-if="result && !submitting">{{ result.content }}</pre>
          <div v-if="!result && !submitting" class="empty-response">
            <el-icon><ChatDotRound /></el-icon>
            <strong>等待一次测试调用</strong>
            <span>响应内容将在这里完整显示</span>
          </div>
        </div>
      </PageCard>
    </div>
  </div>
</template>

<style scoped>
.llm-page { display: flex; flex-direction: column; gap: 22px; }
.page-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; padding: 4px; }
.page-heading h1 { margin-top: 10px; font-size: 28px; }
.page-heading p { margin: 8px 0 0; color: var(--text-muted); }
.workspace { display: grid; grid-template-columns: minmax(420px, .9fr) minmax(440px, 1.1fr); gap: 18px; align-items: start; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; min-height: 340px; }
.field { display: flex; flex-direction: column; gap: 7px; min-width: 0; }
.field > span { color: var(--text-strong); font-size: 12px; font-weight: 600; }
.field em { color: var(--err-color); font-style: normal; margin-left: 4px; }
.field small { color: var(--text-faint); font-size: 11px; }
.field :deep(.el-input-number), .field :deep(.el-select) { width: 100%; }
.transport-options { display: flex; width: 100%; }
.transport-options :deep(.el-radio-button) { flex: 1; }
.transport-options :deep(.el-radio-button__inner) { width: 100%; }
.selected-value-highlight :deep(.el-select__selected-item) {
  color: var(--primary-700) !important;
  font-weight: var(--fw-bold);
}
:root.dark .selected-value-highlight :deep(.el-select__selected-item) { color: var(--primary-300) !important; }
.auth-notice {
  grid-column: 1 / -1;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 11px 13px;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
  background: var(--bg-sunken);
}
.auth-notice > .el-icon { margin-top: 2px; font-size: 17px; flex-shrink: 0; }
.auth-notice > div { display: flex; flex-direction: column; gap: 2px; }
.auth-notice strong { font-size: 12px; }
.auth-notice span { color: var(--text-muted); font-size: 11px; line-height: 1.5; }
.auth-notice.is-strict { background: var(--warn-soft); border-color: var(--warn-line); }
.auth-notice.is-strict > .el-icon, .auth-notice.is-strict strong { color: var(--warn-color); }
.auth-notice.is-relaxed { background: var(--info-soft); border-color: var(--info-line); }
.auth-notice.is-relaxed > .el-icon, .auth-notice.is-relaxed strong { color: var(--primary-600); }
.message-field { grid-column: 1 / -1; }
.message-field :deep(.el-textarea__inner) { min-height: 190px !important; line-height: 1.65; }
.submit-btn { grid-column: 1 / -1; width: 100%; }
.submit-btn .spinner { width: 15px; height: 15px; border-color: rgba(255,255,255,.45); border-top-color: #fff; }
.response { min-height: 512px; border-radius: var(--radius-lg); border: 1px solid var(--hairline); background: var(--bg-sunken); padding: 18px; }
.response.empty { display: grid; place-items: center; }
.response.is-loading { overflow: hidden; }
.response-loading { min-height: 474px; display: flex; flex-direction: column; }
.loading-meta { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding-bottom: 14px; border-bottom: 1px solid var(--hairline); color: var(--text-faint); font: 11px 'JetBrains Mono', monospace; }
.loading-state { display: inline-flex; align-items: center; gap: 7px; color: var(--primary-600); font-family: 'Plus Jakarta Sans', system-ui, sans-serif; font-size: 12px; font-weight: var(--fw-semibold); }
.loading-dot { width: 7px; height: 7px; border-radius: 50%; background: var(--primary-500); box-shadow: 0 0 0 0 rgba(20, 184, 166, .35); animation: loading-pulse 1.6s var(--ease) infinite; }
.loading-body { display: flex; flex-direction: column; align-items: center; gap: 10px; max-width: 430px; margin: 68px auto 48px; text-align: center; }
.loading-body strong { color: var(--text-strong); font-size: 15px; font-weight: var(--fw-semibold); }
.loading-body > span { color: var(--text-muted); font-size: 12px; line-height: 1.7; text-wrap: balance; }
.loading-wave { display: flex; align-items: center; justify-content: center; gap: 4px; width: 54px; height: 36px; margin-bottom: 4px; }
.loading-wave span { width: 4px; height: 26px; border-radius: 999px; background: var(--primary-500); transform: scaleY(.35); opacity: .4; animation: loading-wave 1.15s ease-in-out infinite; }
.loading-wave span:nth-child(2), .loading-wave span:nth-child(4) { animation-delay: -0.18s; }
.loading-wave span:nth-child(3) { animation-delay: -0.36s; }
.loading-skeleton { display: flex; flex-direction: column; gap: 12px; margin-top: auto; padding: 0 8px 12px; }
.skeleton-line { position: relative; display: block; height: 10px; overflow: hidden; border-radius: 999px; background: var(--bg-deep); }
.skeleton-line::after { content: ''; position: absolute; inset: 0; background: linear-gradient(90deg, transparent, var(--info-soft), transparent); transform: translateX(-100%); animation: loading-sweep 1.8s var(--ease) infinite; }
.skeleton-line.line-short { width: 34%; }
.skeleton-line.line-medium { width: 68%; }
.skeleton-line.line-long { width: 92%; }
:root.dark .loading-state { color: var(--primary-300); }
@keyframes loading-pulse { 0%, 100% { box-shadow: 0 0 0 0 rgba(20, 184, 166, .35); opacity: 1; } 50% { box-shadow: 0 0 0 7px rgba(20, 184, 166, 0); opacity: .72; } }
@keyframes loading-wave { 0%, 100% { transform: scaleY(.35); opacity: .35; } 50% { transform: scaleY(1); opacity: 1; } }
@keyframes loading-sweep { to { transform: translateX(100%); } }
.response-meta { display: flex; align-items: center; justify-content: space-between; padding-bottom: 14px; border-bottom: 1px solid var(--hairline); color: var(--text-faint); font: 11px 'JetBrains Mono', monospace; }
.response-status { display: flex; align-items: center; gap: 9px; min-width: 0; }
.response-code { overflow-wrap: anywhere; }
.response-info { margin-top: 16px; padding: 10px 12px; border: 1px solid var(--hairline); border-radius: var(--radius-md); font-size: 12px; line-height: 1.6; }
.response-info.is-success { color: var(--ok-color); background: var(--ok-soft); border-color: var(--ok-line); }
.response-info.is-error { color: var(--err-color); background: var(--err-soft); border-color: var(--err-line); }
.response pre { margin: 18px 0 0; white-space: pre-wrap; overflow-wrap: anywhere; color: var(--text-default); font: 13px/1.75 'JetBrains Mono', monospace; }
.empty-response { display: flex; flex-direction: column; align-items: center; gap: 8px; color: var(--text-faint); text-align: center; }
.empty-response .el-icon { font-size: 34px; color: var(--primary-500); }
.empty-response strong { color: var(--text-default); }
.empty-response span { font-size: 12px; }
@media (prefers-reduced-motion: reduce) { .loading-dot, .loading-wave span, .skeleton-line::after { animation: none; } }
@media (max-width: 1080px) { .workspace { grid-template-columns: 1fr; } .response { min-height: 320px; } }
@media (max-width: 640px) { .form-grid { grid-template-columns: 1fr; } .message-field, .submit-btn { grid-column: auto; } .page-heading { align-items: flex-start; } .response-meta, .loading-meta { align-items: flex-start; flex-direction: column; gap: 10px; } .loading-body { margin: 48px auto 38px; } }
</style>
