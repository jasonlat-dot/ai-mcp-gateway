<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    class="auth-form"
    hide-required-asterisk
    @submit.prevent
  >
    <el-form-item prop="username" class="stagger" style="--i:0">
      <el-input
        v-model="form.username"
        placeholder="请输入用户名"
        size="large"
        :prefix-icon="User"
        clearable
        @focus="onUsernameFocus"
        @blur="onUsernameBlur"
        @input="onUsernameInput"
      />
    </el-form-item>

    <el-form-item prop="phone" class="stagger" style="--i:1">
      <el-input
        v-model="form.phone"
        placeholder="请输入手机号"
        size="large"
        :prefix-icon="Phone"
        maxlength="11"
        @focus="onPhoneFocus"
        @blur="onPhoneBlur"
        @input="onPhoneInput"
      />
    </el-form-item>

    <el-form-item prop="password" class="stagger" style="--i:2">
      <el-input
        v-model="form.password"
        placeholder="请输入密码 (8-16位，字母+数字)"
        size="large"
        :prefix-icon="Lock"
        :type="showPwd ? 'text' : 'password'"
        @focus="onPasswordFocus"
        @blur="onPasswordBlur"
        @input="onPasswordInput"
      >
        <template #suffix>
          <span class="eye" @click="toggleShowPwd">
            <el-icon><component :is="showPwd ? View : Hide" /></el-icon>
          </span>
        </template>
      </el-input>
      <div v-if="pwdHint" class="hint" :class="pwdHintClass">{{ pwdHint }}</div>
    </el-form-item>

    <el-form-item prop="confirm" class="stagger" style="--i:3">
      <el-input
        v-model="form.confirm"
        placeholder="请再次输入密码"
        size="large"
        :prefix-icon="Lock"
        :type="showConfirm ? 'text' : 'password'"
        @focus="onConfirmFocus"
        @blur="onConfirmBlur"
        @input="onConfirmInput"
      >
        <template #suffix>
          <span class="eye" @click="toggleShowConfirm">
            <el-icon><component :is="showConfirm ? View : Hide" /></el-icon>
          </span>
        </template>
      </el-input>
      <div v-if="confirmHint" class="hint" :class="confirmHintClass">{{ confirmHint }}</div>
    </el-form-item>

    <el-form-item prop="agree" class="stagger" style="--i:4">
      <el-checkbox v-model="form.agree">
        我已阅读并同意 <a class="link" @click.stop>《用户协议》</a>
      </el-checkbox>
    </el-form-item>

    <el-form-item class="stagger" style="--i:5">
      <StreamButton :loading="loading" @click="handleSubmit">
        {{ loading ? '注册中…' : '注 册' }}
      </StreamButton>
    </el-form-item>

    <div class="alt-tip stagger" style="--i:6">
      已有账号？<a class="link" @click="$emit('switch', 'login')">前往登录</a>
    </div>
  </el-form>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { User, Lock, Phone, View, Hide } from '@element-plus/icons-vue'
import StreamButton from '@/components/ui/StreamButton.vue'

const props = defineProps({
  loading: { type: Boolean, default: false },
})
const emit = defineEmits(['submit', 'switch', 'scene'])

const formRef = ref(null)
const showPwd = ref(false)
const showConfirm = ref(false)

const form = reactive({
  username: '',
  phone: '',
  password: '',
  confirm: '',
  agree: false,
})

/* ===== 角色动画场景状态 (对齐 LoginForm 的语义) ===== */
const usernameFocused = ref(false)
const phoneFocused    = ref(false)
const passwordFocused = ref(false)
const confirmFocused  = ref(false)

function emitScene(extra = {}) {
  emit('scene', {
    typingUsername: usernameFocused.value || phoneFocused.value,
    typingPassword: passwordFocused.value || confirmFocused.value,
    showPassword:   showPwd.value || showConfirm.value,
    passwordLength: (form.password || '').length + (form.confirm || '').length,
    ...extra,
  })
}

function onUsernameFocus() {
  usernameFocused.value = true
  triggerValidate('username')
  emitScene()
}
function onUsernameBlur() {
  usernameFocused.value = false
  triggerValidate('username')
  emitScene()
}
function onUsernameInput() {
  clearFieldErr('username')
  emitScene()
}

function onPhoneFocus() {
  phoneFocused.value = true
  triggerValidate('phone')
  emitScene()
}
function onPhoneBlur() {
  phoneFocused.value = false
  triggerValidate('phone')
  emitScene()
}
function onPhoneInput() {
  clearFieldErr('phone')
  emitScene()
}

function onPasswordFocus() {
  passwordFocused.value = true
  triggerValidate('password')
  emitScene()
}
function onPasswordBlur() {
  passwordFocused.value = false
  triggerValidate('password')
  emitScene()
}
function onPasswordInput() {
  clearFieldErr('password')
  emitScene()
}

function onConfirmFocus() {
  confirmFocused.value = true
  triggerValidate('confirm')
  emitScene()
}
function onConfirmBlur() {
  confirmFocused.value = false
  triggerValidate('confirm')
  emitScene()
}
function onConfirmInput() {
  clearFieldErr('confirm')
  emitScene()
}

function toggleShowPwd() {
  showPwd.value = !showPwd.value
  emitScene()
}
function toggleShowConfirm() {
  showConfirm.value = !showConfirm.value
  emitScene()
}

/* 自定义校验 */
const validatePasswordStrength = (_, value, cb) => {
  if (!value) return cb(new Error('请输入密码'))
  if (value.length < 8 || value.length > 16) return cb(new Error('密码长度需 8-16 位'))
  const hasLetter = /[A-Za-z]/.test(value)
  const hasDigit  = /[0-9]/.test(value)
  if (!hasLetter || !hasDigit) return cb(new Error('密码必须同时包含字母和数字'))
  cb()
}

const validateConfirm = (_, value, cb) => {
  if (!value) return cb(new Error('请再次输入密码'))
  if (value !== form.password) return cb(new Error('两次输入的密码不一致'))
  cb()
}

const validateAgree = (_, value, cb) => {
  if (!value) return cb(new Error('请勾选用户协议'))
  cb()
}

const validatePhone = (_, value, cb) => {
  if (!value) return cb(new Error('请输入手机号'))
  if (!/^1[3-9]\d{9}$/.test(value)) return cb(new Error('请输入有效的 11 位手机号'))
  cb()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone:    [{ validator: validatePhone,    trigger: 'blur' }],
  password: [{ validator: validatePasswordStrength, trigger: 'blur' }],
  confirm:  [{ validator: validateConfirm,  trigger: 'blur' }],
  agree:    [{ validator: validateAgree,    trigger: 'change' }],
}

/* 实时提示 */
const pwdHint = computed(() => {
  const v = form.password
  if (!v) return ''
  if (v.length < 8) return `还需 ${8 - v.length} 位达到最低长度`
  if (v.length > 16) return '超过最大长度 16 位'
  if (!/[A-Za-z]/.test(v)) return '还需包含字母'
  if (!/[0-9]/.test(v))   return '还需包含数字'
  return '密码强度符合要求 ✓'
})
const pwdHintClass = computed(() => {
  const v = form.password
  if (!v) return ''
  const ok = v.length >= 8 && v.length <= 16 && /[A-Za-z]/.test(v) && /[0-9]/.test(v)
  return ok ? 'hint-ok' : 'hint-warn'
})

const confirmHint = computed(() => {
  const v = form.confirm
  if (!v) return ''
  if (v !== form.password) return '两次密码不一致'
  return '两次密码一致 ✓'
})
const confirmHintClass = computed(() => {
  const v = form.confirm
  if (!v) return ''
  return v === form.password ? 'hint-ok' : 'hint-err'
})

function triggerValidate(field) {
  formRef.value?.validateField(field)
}
function clearFieldErr(field) {
  formRef.value?.clearValidate([field])
}

function focusFirstError() {
  const errEl = document.querySelector('.auth-form .el-form-item.is-error input')
  if (errEl) errEl.focus()
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    focusFirstError()
    return
  }
  emit('submit', { ...form })
}
</script>

<style scoped>
.auth-form { display: flex; flex-direction: column; }

.stagger {
  opacity: 0;
  transform: translateY(10px);
  animation: fadeUp 0.5s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
  animation-delay: calc(var(--i) * 60ms + 80ms);
}
@keyframes fadeUp {
  to { opacity: 1; transform: translateY(0); }
}

.hint {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1;
  padding-left: 2px;
  transition: color 0.3s;
}
.hint-warn { color: #fbbf24; }
.hint-err  { color: #f43f5e; }
.hint-ok   { color: #34d399; }

.alt-tip {
  text-align: center;
  font-size: 13px;
  color: rgba(30, 41, 59, 0.65);
  margin-top: 8px;
}

.eye {
  display: inline-flex; align-items: center;
  padding: 6px;
  cursor: pointer;
  color: rgba(30, 41, 59, 0.55);
  transition: color 0.2s;
}
.eye:hover { color: #0284c7; }

.link {
  color: #0ea5e9;
  cursor: pointer;
  transition: color 0.2s;
}
.link:hover { color: #0284c7; text-decoration: underline; }

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.95) !important;
  border-radius: 10px !important;
  box-shadow: 0 0 0 1px rgba(186, 230, 253, 0.6) inset !important;
  transition: all 0.3s ease !important;
  padding: 4px 12px !important;
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(125, 211, 252, 0.85) inset !important;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px #38bdf8 inset,
    0 0 0 4px rgba(56, 189, 248, 0.16),
    0 0 18px rgba(56, 189, 248, 0.25) !important;
  background: rgba(255, 255, 255, 1) !important;
}
:deep(.el-input__inner) {
  color: #1e293b !important;
  height: 42px;
  font-size: 14px;
}
:deep(.el-input__inner::placeholder) { color: rgba(30, 41, 59, 0.42); }
:deep(.el-input__prefix .el-icon),
:deep(.el-input__suffix .el-icon) { color: rgba(30, 41, 59, 0.55); }

:deep(.el-form-item__error) {
  color: #f43f5e !important;
  font-size: 12px;
  padding-top: 2px;
}

:deep(.el-checkbox__label) { color: rgba(30, 41, 59, 0.78); }
:deep(.el-checkbox__inner) {
  background: rgba(255, 255, 255, 0.9) !important;
  border-color: rgba(56, 189, 248, 0.5) !important;
}
:deep(.el-checkbox.is-checked .el-checkbox__inner) {
  background: linear-gradient(120deg, #5eead4 0%, #6ee7b7 45%, #fcd34d 100%) !important;
  border-color: transparent !important;
}
</style>
