<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const auth  = useAuthStore()
const route = useRoute()
const router = useRouter()
const toast = useToast()

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
    toast.success('登录成功,正在进入控制台…', { duration: 1800 })
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (e) {
    errors.password = e?.message || '账号或密码不正确'
    toast.error(e?.message || '登录失败', { duration: 2200 })
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
          <svg viewBox="0 0 28 28" width="20" height="20" aria-hidden="true">
            <defs>
              <linearGradient id="bm" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%"  stop-color="#14b8a6"/>
                <stop offset="100%" stop-color="#0d9488"/>
              </linearGradient>
            </defs>
            <path d="M14 2 L26 8.5 V19.5 L14 26 L2 19.5 V8.5 Z"
                  fill="none" stroke="url(#bm)" stroke-width="1.6"/>
            <circle cx="14" cy="14" r="3.4" fill="url(#bm)"/>
          </svg>
          <span>Mcp gateway</span>
        </div>

        <h1 class="brand-title">
          <span>MCP 网关</span>
          <span class="text-gradient">运营控制台</span>
        </h1>

        <p class="brand-desc">
          统一管理 AI 网关、工具、协议与认证配置。
          以最少的点击,把最关键的网关治理摆在你面前。
        </p>

        <ul class="feature-list">
          <li class="card animate-slide-up" style="animation-delay: 0ms">
            <span class="feat-icon"><el-icon><Connection /></el-icon></span>
            <div>
              <strong>实时网关拓扑</strong>
              <small>查看每个网关下的工具、协议、认证状态</small>
            </div>
          </li>
          <li class="card animate-slide-up" style="animation-delay: 80ms">
            <span class="feat-icon feat-icon--violet"><el-icon><Document /></el-icon></span>
            <div>
              <strong>OpenAPI 一键导入</strong>
              <small>解析 → 映射 → 入库,分钟级接入</small>
            </div>
          </li>
          <li class="card animate-slide-up" style="animation-delay: 160ms">
            <span class="feat-icon feat-icon--teal"><el-icon><Lightning /></el-icon></span>
            <div>
              <strong>限流与 API Key</strong>
              <small>在控制台即时发放、回收、调整速率</small>
            </div>
          </li>
        </ul>

        <div class="brand-foot">
          <span class="badge badge-success"><span class="dot" />所有服务正常</span>
          <span class="version">v1.0.0 · {{ year }}</span>
        </div>
      </section>

      <!-- 右侧登录卡片 -->
      <section class="form-shell">
        <div class="card-shell card-glass animate-scale-in">
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
                <a class="muted-link" href="javascript:;" @click.prevent="toast.warning('请联系系统管理员')">忘记密码?</a>
              </div>

              <button class="btn btn-primary btn-lg btn-block submit-btn" :disabled="submitDisabled" type="submit">
                <span v-if="!loading">进入控制台 <el-icon><ArrowRight /></el-icon></span>
                <span v-else class="loading">
                  <span class="spinner" />登录中…
                </span>
              </button>

              <button type="button" class="btn btn-secondary quick-fill" @click="quickFill">
                <el-icon><Key /></el-icon>
                使用测试账号一键填入
                <span class="muted">( admin / password123 )</span>
              </button>
            </form>
          </div>
        </div>

        <p class="copyright">
          © {{ year }} AI MCP Gateway
        </p>
      </section>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100dvh;
  width: 100%;
  display: grid;
  place-items: center;
  background: var(--bg-base);
  background-image: var(--bg-mesh);
  padding: 32px 16px;
  overflow: hidden;
  animation: fadeIn 0.4s ease-out;
}
.auth-page::before, .auth-page::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
}
.auth-page::before {
  width: 480px; height: 480px;
  top: -100px; left: -100px;
  background: rgba(20, 184, 166, 0.20);
}
.auth-page::after {
  width: 540px; height: 540px;
  bottom: -120px; right: -120px;
  background: rgba(6, 182, 212, 0.18);
}
:root.dark .auth-page::before { background: rgba(20, 184, 166, 0.25); }
:root.dark .auth-page::after  { background: rgba(6, 182, 212, 0.20); }

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
  animation: slideUp 0.4s ease-out;
}
.brand-mark {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 7px 14px;
  border-radius: var(--radius-pill);
  background: var(--bg-elevated);
  border: 1px solid var(--hairline);
  width: max-content;
  font-weight: var(--fw-bold);
  font-size: var(--fs-sm);
  letter-spacing: var(--ls-snug);
  color: var(--text-strong);
  box-shadow: var(--shadow-sm);
}

.brand-title {
  display: flex;
  flex-direction: column;
  font-size: clamp(36px, 5vw, 54px);
  font-weight: var(--fw-bold);
  line-height: 1.05;
  letter-spacing: var(--ls-tight);
  color: var(--text-strong);
}
.brand-desc {
  font-size: var(--fs-lg);
  color: var(--text-muted);
  line-height: var(--lh-relaxed);
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
  border-radius: var(--radius-xl);
  border: 1px solid var(--hairline);
  background: var(--bg-elevated);
  align-items: flex-start;
  box-shadow: var(--shadow-card);
  transition: all var(--dur-base) var(--ease-glacis);
}
.feature-list li:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--info-line);
}

.feat-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--info-soft);
  border: 1px solid var(--info-line);
  color: var(--primary-600);
  display: grid;
  place-items: center;
  font-size: var(--fs-2xl);
  flex-shrink: 0;
}
:root.dark .feat-icon { color: var(--primary-300); }
.feat-icon--violet { background: var(--violet-soft); border-color: rgba(139, 92, 246, 0.28); color: var(--violet-color); }
.feat-icon--teal   { background: var(--info-soft);   border-color: var(--info-line);          color: var(--primary-600); }
:root.dark .feat-icon--teal { color: var(--primary-300); }

.feature-list li strong {
  display: block;
  color: var(--text-strong);
  font-weight: var(--fw-semibold);
  font-size: var(--fs-base);
  margin-bottom: 2px;
}
.feature-list li small {
  color: var(--text-muted);
  font-size: var(--fs-xs);
}

.brand-foot { display: flex; align-items: center; gap: 12px; margin-top: 12px; }
.brand-foot .dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: currentColor;
  margin-right: 4px;
  display: inline-block;
  animation: pulseSoft 2s ease-in-out infinite;
}
.version {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--fs-2xs);
  letter-spacing: var(--ls-wide);
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
  background: var(--bg-elevated);
  border: 1px solid var(--hairline-strong);
  border-radius: var(--radius-2xl);
  padding: 0;
  box-shadow: var(--shadow-xl);
  width: 100%;
  max-width: 460px;
  overflow: hidden;
}

.card-inner { padding: 32px 32px 28px; }

.card-head { margin-bottom: 22px; }
.card-head h2 {
  font-size: var(--fs-3xl);
  font-weight: var(--fw-bold);
  margin-top: 8px;
  letter-spacing: var(--ls-tight);
  color: var(--text-strong);
}
.card-head p {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: var(--fs-sm);
  line-height: var(--lh-base);
}

/* ===== Form ===== */
.auth-form { display: flex; flex-direction: column; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 8px; }
.field label {
  font-size: var(--fs-xs);
  font-weight: var(--fw-semibold);
  letter-spacing: var(--ls-wide);
  color: var(--text-default);
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  height: 48px;
  border-radius: var(--radius-lg);
  background: var(--input-bg);
  border: 1px solid var(--input-border);
  transition: all var(--dur-base) var(--ease-glacis);
}
.input-wrap:focus-within {
  border-color: var(--input-border-focus);
  box-shadow: var(--ring-focus);
}
.input-icon { font-size: var(--fs-xl); color: var(--text-muted); flex-shrink: 0; }
.input-wrap input {
  flex: 1;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--input-text);
  font-size: var(--fs-base);
  font-weight: var(--fw-medium);
  font-family: inherit;
}
.input-wrap input::placeholder { color: var(--input-placeholder); font-weight: var(--fw-regular); }

.field.has-error .input-wrap {
  border-color: var(--err-color);
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15);
}
.field-tip { font-size: var(--fs-xs); color: var(--err-color); }

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
  font-size: var(--fs-sm);
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
  background: var(--input-bg);
  transition: all var(--dur-base) var(--ease-glacis);
  color: #ffffff;
  font-size: var(--fs-2xs);
}
.check-box.on {
  background: var(--primary-500);
  border-color: var(--primary-500);
}

.muted-link {
  font-size: var(--fs-sm);
  color: var(--text-muted);
  transition: color var(--dur-fast) var(--ease-glacis);
}
.muted-link:hover { color: var(--primary-600); }
:root.dark .muted-link:hover { color: var(--primary-300); }

.submit-btn { margin-top: 8px; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.submit-btn .loading { display: inline-flex; align-items: center; gap: 10px; }

.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 720ms linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.quick-fill {
  height: auto;
  padding: 10px 14px;
  border-style: dashed;
  color: var(--text-muted);
  font-size: var(--fs-sm);
}
.quick-fill:hover {
  color: var(--primary-600);
  border-color: var(--info-line);
  background: var(--info-soft);
}
:root.dark .quick-fill:hover { color: var(--primary-300); }
.quick-fill .muted {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--fs-xs);
  color: var(--text-faint);
}

.copyright {
  font-size: var(--fs-xs);
  color: var(--text-faint);
  text-align: center;
  margin-top: 4px;
}
</style>
