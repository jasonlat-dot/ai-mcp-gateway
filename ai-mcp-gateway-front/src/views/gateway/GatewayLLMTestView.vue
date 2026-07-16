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
const result = ref('')
const elapsed = ref(null)

const form = reactive({
  gatewayId: '',
  authApiKey: '',
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

watch(() => form.gatewayId, () => {
  if (!gatewayAuths.value.some((item) => item.apiKey === form.authApiKey)) {
    form.authApiKey = gatewayAuths.value[0]?.apiKey || ''
  }
  result.value = ''
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
  result.value = ''
  elapsed.value = null
  const startedAt = performance.now()
  try {
    const data = await testCallGateway({
      gatewayId: form.gatewayId,
      authApiKey: form.authApiKey || null,
      timeout: Number(form.timeout),
      message: form.message.trim(),
    })
    result.value = data.content || ''
    elapsed.value = Math.round(performance.now() - startedAt)
    toast.success('网关调用成功', { duration: 1800 })
  } catch (error) {
    elapsed.value = Math.round(performance.now() - startedAt)
    toast.error(error.message || '网关调用失败')
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
        <p>选择网关及其认证 Key，发送自然语言请求验证 MCP 工具调用链路。</p>
      </div>
      <StatusPill :tone="submitting ? 'warn' : 'info'">{{ submitting ? '调用中' : '准备就绪' }}</StatusPill>
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

      <PageCard eyebrow="Response" title="LLM 响应" desc="接口返回的 content 原文">
        <div class="response" :class="{ empty: !result }">
          <div v-if="result" class="response-meta">
            <StatusPill tone="ok">调用成功</StatusPill>
            <span>{{ elapsed }} ms</span>
          </div>
          <pre v-if="result">{{ result }}</pre>
          <div v-else class="empty-response">
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
.response-meta { display: flex; align-items: center; justify-content: space-between; padding-bottom: 14px; border-bottom: 1px solid var(--hairline); color: var(--text-faint); font: 11px 'JetBrains Mono', monospace; }
.response pre { margin: 18px 0 0; white-space: pre-wrap; overflow-wrap: anywhere; color: var(--text-default); font: 13px/1.75 'JetBrains Mono', monospace; }
.empty-response { display: flex; flex-direction: column; align-items: center; gap: 8px; color: var(--text-faint); text-align: center; }
.empty-response .el-icon { font-size: 34px; color: var(--primary-500); }
.empty-response strong { color: var(--text-default); }
.empty-response span { font-size: 12px; }
@media (max-width: 1080px) { .workspace { grid-template-columns: 1fr; } .response { min-height: 320px; } }
@media (max-width: 640px) { .form-grid { grid-template-columns: 1fr; } .message-field, .submit-btn { grid-column: auto; } .page-heading { align-items: flex-start; } }
</style>
