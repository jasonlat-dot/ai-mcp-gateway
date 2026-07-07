<template>
  <StatusPage
    icon="🛡️"
    code="403"
    glow-color="#f672ca"
    gradient-from="#fbc2eb"
    gradient-to="#a18cd1"
    title="暂无访问权限"
    description="你的账号已登录,但当前角色无权访问该资源。"
    :actions="actions"
    :tips="tips"
  >
    <div>当前角色:<span class="role">{{ role || 'guest' }}</span></div>
  </StatusPage>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import StatusPage from './StatusPage.vue'

const props = defineProps({
  // 当前角色(可由父级通过 props 传入,默认从 store/localStorage 取)
  role: { type: String, default: '' },
  // 联系管理员的入口(可选)
  contactUrl: { type: String, default: '' },
})

const router = useRouter()

function goHome()      { router.push('/') }
function requestAccess(){
  if (props.contactUrl) window.open(props.contactUrl, '_blank')
  else router.push('/apply')
}

const tips = computed(() => [
  '联系管理员申请权限',
  '切换有权限的账号',
  '返回安全的工作台',
])

const actions = computed(() => [
  { text: '返回首页',     type: 'primary', onClick: goHome },
  { text: '申请权限',     type: 'ghost',   onClick: requestAccess },
])
</script>

<style scoped>
.role {
  display: inline-block;
  margin-left: 6px;
  padding: 2px 10px;
  background: linear-gradient(120deg, #6a7bff, #a18cd1);
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 1px;
  color: #fff;
}
</style>