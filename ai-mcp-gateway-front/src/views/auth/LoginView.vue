<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const auth  = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  remember: true,
})

const loading = ref(false)
const errors  = reactive({ username: '', password: '' })

const submitDisabled = computed(() => loading.value || !form.username || !form.password)

function validate() {
  errors.username = form.username ? '' : '请输入账号'
  errors.password = !form.password ? '请输入密码' : (form.password.length < 6 ? '密码长度不少于 6 位' : '')
  return !errors.username && !errors.password
}

async function onSubmit() {
  if (!validate()) return
  loading.value = true
  try {
    await auth.login({
      username: form.username.trim(),
      password: form.password,
    })
    ElMessage.success('登录成功,正在进入控制台…')
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (e) {
    errors.password = e?.message || '账号或密码不正确'
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function quickFill() {
  form.username = 'admin'
  form.password = 'password123'
}

const year = new Date().getFullYear()
</script>

<template>
  <div class="auth-page">
    <div class="auth-grid">
      <!-- 左侧品牌叙事 -->
      <section class="brand">
        <div class="brand-mark">
          <svg viewBox="0 0 28 28" width="20" height="20">
            <defs>
              <linearGradient id="bm" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%"  stop-color="#2563eb"/>
                <stop offset="100%" stop-color="#0d9488"/>
              </linearGradient>
            </defs>
            <path d="M14 2 L26 8.5 V19.5 L14 26 L2 19.5 V8.5 Z"
                  fill="none" stroke="url(#bm)" stroke-width="1.6"/>
            <circle cx="14" cy="14" r="3.4" fill="url(#bm)"/>
          </svg>
          <span>Glacis</span>
        </div>

        <h1 class="brand-title">
          <span>MCP 网关</span>
          <span class="text-accent">运营控制台</span>
        </h1>

        <p class="brand-desc">
          统一管理 AI 网关、工具、协议与认证配置。
          以最少的点击,把最关键的网关治理摆在你面前。
        </p>

        <ul class="feature-list">
          <li>
            <span class="feat-icon"><el-icon><Connection /></el-icon></span>
            <div>
              <strong>实时网关拓扑</strong>
              <small>查看每个网关下的工具、协议、认证状态</small>
            </div>
          </li>
          <li>
            <span class="feat-icon feat-icon--violet"><el-icon><Document /></el-icon></span>
            <div>
              <strong>OpenAPI 一键导入</strong>
              <small>解析 → 映射 → 入库,分钟级接入</small>
            </div>
          </li>
          <li>
            <span class="feat-icon feat-icon--teal"><el-icon><Lightning /></el-icon></span>
            <div>
              <strong>限流与 API Key</strong>
              <small>在控制台即时发放、回收、调整速率</small>
            </div>
          </li>
        </ul>

        <div class="brand-foot">
          <span class="status-pill"><span class="dot" />所有服务正常</span>
          <span class="version">v1.0.0 · {{ year }}</span>
        </div>
      </section>

      <!-- 右侧登录卡片 -->
      <section class="form-shell">
        <div class="card-shell">
          <div class="card-inner">
            <header class="card-head">
              <span class="eyebrow">Sign in</span>
              <h2>欢迎回来</h2>
              <p>使用您的管理员账号登录,继续管理工作台。</p>
            </header>

            <form class="auth-form" @submit.prevent="onSubmit">
              <div class="field" :class="{ 'has-error': errors.username }">
                <label>账号</label>
                <div class="input-wrap">
                  <el-icon class="input-icon"><User /></el-icon>
                  <input
                    v-model="form.username"
                    type="text"
                    placeholder="请输入账号"
                    autocomplete="username"
                    spellcheck="false"
                    @input="errors.username = ''"
                  />
                </div>
                <span v-if="errors.username" class="field-tip">{{ errors.username }}</span>
              </div>

              <div class="field" :class="{ 'has-error': errors.password }">
                <label>密码</label>
                <div class="input-wrap">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <input
                    v-model="form.password"
                    type="password"
                    placeholder="请输入密码"
                    autocomplete="current-password"
                    @input="errors.password = ''"
                  />
                </div>
                <span v-if="errors.password" class="field-tip">{{ errors.password }}</span>
              </div>

              <div class="row-between">
                <label class="check">
                  <input v-model="form.remember" type="checkbox" />
                  <span class="check-box" :class="{ on: form.remember }">
                    <el-icon v-if="form.remember"><Check /></el-icon>
                  </span>
                  <span>记住我</span>
                </label>
                <a class="muted-link" href="javascript:;" @click.prevent="ElMessage.warning('请联系系统管理员')">忘记密码?</a>
              </div>

              <button class="submit-btn" :disabled="submitDisabled" type="submit">
                <span v-if="!loading">进入控制台 <el-icon><ArrowRight /></el-icon></span>
                <span v-else class="loading">
                  <span class="spinner" />登录中…
                </span>
              </button>

              <button type="button" class="quick-fill" @click="quickFill">
                <el-icon><Key /></el-icon>
                使用测试账号一键填入
                <span class="muted">( admin / password123 )</span>
              </button>
            </form>
          </div>
        </div>

        <p class="copyright">
          © {{ year }} AI MCP Gateway · 由 Glacis UI 驱动
        </p>
      </section>
    </div>
  </div>
</template>

<style scoped lang="scss">
.auth-page {
  position: relative;
  min-height: 100dvh;
  width: 100%;
  display: grid;
  place-items: center;
  background:
    radial-gradient(1200px 600px at 0% 0%, rgba(37, 99, 235, 0.06) 0%, transparent 60%),
    radial-gradient(900px 500px at 100% 100%, rgba(13, 148, 136, 0.05) 0%, transparent 65%),
    var(--bg-base);
  padding: 32px 16px;
  overflow: hidden;
}

.auth-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 56px;
  max-width: 1200px;
  width: 100%;
  align-items: center;
}

@media (max-width: 980px) {
  .auth-grid { grid-template-columns: 1fr; gap: 28px; }
}

/* ===== Brand ===== */
.brand {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 18px;
  max-width: 520px;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 7px 14px;
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid var(--hairline);
  width: max-content;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--text-strong);
}

.brand-title {
  display: flex;
  flex-direction: column;
  font-size: clamp(36px, 5vw, 54px);
  font-weight: 700;
  line-height: 1.05;
  letter-spacing: -0.035em;
  color: var(--text-strong);
}

.brand-desc {
  font-size: 15px;
  color: var(--text-muted);
  line-height: 1.65;
  max-width: 480px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 4px;
}

.feature-list li {
  display: flex;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid var(--hairline);
  background: #ffffff;
  align-items: flex-start;
  box-shadow: var(--shadow-xs);
}

.feat-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--accent-soft);
  border: 1px solid var(--accent-line);
  color: var(--accent);
  display: grid;
  place-items: center;
  font-size: 18px;
  flex-shrink: 0;
}

.feat-icon--violet {
  background: var(--violet-soft);
  border-color: rgba(109, 85, 224, 0.22);
  color: var(--violet);
}

.feat-icon--teal {
  background: var(--teal-soft);
  border-color: rgba(13, 148, 136, 0.22);
  color: var(--teal);
}

.feature-list li strong {
  display: block;
  color: var(--text-strong);
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 2px;
}

.feature-list li small {
  color: var(--text-muted);
  font-size: 12px;
}

.brand-foot {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--teal);
  background: var(--teal-soft);
  border-radius: 999px;
  border: 1px solid rgba(13, 148, 136, 0.22);
}

.status-pill .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--teal);
}

.version {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  letter-spacing: 0.04em;
  color: var(--text-faint);
}

/* ===== Form card ===== */
.form-shell {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.card-shell {
  background: #ffffff;
  border: 1px solid var(--hairline-strong);
  border-radius: var(--radius-xl);
  padding: 1px;
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 460px;
}

.card-inner {
  background: #ffffff;
  border-radius: calc(var(--radius-xl) - 1px);
  padding: 32px 32px 28px;
}

.card-head { margin-bottom: 22px; }
.card-head h2 {
  font-size: 22px;
  font-weight: 700;
  margin-top: 8px;
  letter-spacing: -0.02em;
  color: var(--text-strong);
}
.card-head p {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.55;
}

/* ===== Form ===== */
.auth-form { display: flex; flex-direction: column; gap: 16px; }

.field { display: flex; flex-direction: column; gap: 8px; }

.field label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--text-default);
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  height: 46px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid var(--input-border);
  transition: all var(--dur-base) var(--ease);
}

.input-wrap:focus-within {
  border-color: var(--input-border-focus);
  box-shadow: var(--ring-focus);
}

.input-icon { font-size: 16px; color: var(--text-muted); flex-shrink: 0; }

.input-wrap input {
  flex: 1;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 500;
}

.input-wrap input::placeholder {
  color: var(--input-placeholder);
  font-weight: 400;
}

.field.has-error .input-wrap {
  border-color: rgba(190, 24, 93, 0.55);
  box-shadow: 0 0 0 3px rgba(190, 24, 93, 0.12);
}

.field-tip {
  font-size: 11.5px;
  color: var(--err-color);
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2px;
}

.check {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 12.5px;
  color: var(--text-muted);
}

.check input { display: none; }

.check-box {
  width: 18px;
  height: 18px;
  border-radius: 5px;
  border: 1.5px solid var(--hairline-strong);
  display: grid;
  place-items: center;
  background: #ffffff;
  transition: all var(--dur-base) var(--ease);
  color: #ffffff;
  font-size: 11px;
}

.check-box.on {
  background: var(--accent);
  border-color: var(--accent);
}

.muted-link {
  font-size: 12.5px;
  color: var(--text-muted);
  transition: color var(--dur-fast) var(--ease);
}
.muted-link:hover { color: var(--accent); }

.submit-btn {
  margin-top: 8px;
  height: 48px;
  border: 0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.01em;
  color: #ffffff;
  background: var(--accent);
  cursor: pointer;
  transition: all var(--dur-base) var(--ease);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 1px 2px rgba(37, 99, 235, 0.20), inset 0 1px 0 rgba(255, 255, 255, 0.10);
}

.submit-btn:hover:not(:disabled) {
  background: var(--accent-hover);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.32), inset 0 1px 0 rgba(255, 255, 255, 0.12);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn .loading {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 720ms linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

.quick-fill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 9px;
  background: var(--bg-sunken);
  border: 1px dashed var(--hairline-strong);
  color: var(--text-muted);
  font-size: 12.5px;
  cursor: pointer;
  transition: all var(--dur-base) var(--ease);
}

.quick-fill:hover {
  background: var(--accent-soft);
  color: var(--accent);
  border-color: var(--accent-line);
}

.quick-fill .muted {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11.5px;
  color: var(--text-faint);
}

.copyright {
  font-size: 11.5px;
  color: var(--text-faint);
  text-align: center;
  margin-top: 4px;
}
</style>