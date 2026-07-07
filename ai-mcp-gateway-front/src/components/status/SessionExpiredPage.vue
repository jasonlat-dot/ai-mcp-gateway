<template>
  <StatusPage
    icon="🔐"
    glow-color="#f6c177"
    gradient-from="#f6c177"
    gradient-to="#ff7a8a"
    title="登录已过期"
    description="出于安全考虑,长时间未操作需要重新登录。"
    :actions="actions"
    :tips="['检查系统时间是否正确', '网络切换后请重新登录', '频繁掉线请反馈']"
  >
    <div>剩余有效时间:<span class="time">{{ remainText }}</span></div>
  </StatusPage>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import StatusPage from './StatusPage.vue'

const props = defineProps({
  // 距过期剩余秒数(可选)
  remainSeconds: { type: Number, default: 0 },
  // 重定向登录页时,回跳地址
  redirect: { type: String, default: '' },
})

const router = useRouter()
const route  = useRoute()
const remain = ref(props.remainSeconds)
let timer = null

const remainText = computed(() => {
  if (remain.value <= 0) return '已过期'
  const m = Math.floor(remain.value / 60)
  const s = remain.value % 60
  return m > 0 ? `${m} 分 ${s} 秒` : `${s} 秒`
})

function goLogin() {
  const back = props.redirect || route.fullPath || '/'
  router.push({ path: '/login', query: { redirect: back } })
}

const actions = [
  { text: '重新登录', type: 'primary', onClick: goLogin },
]

onMounted(() => {
  if (remain.value > 0) {
    timer = setInterval(() => {
      remain.value -= 1
      if (remain.value <= 0) clearInterval(timer)
    }, 1000)
  }
})
onBeforeUnmount(() => { timer && clearInterval(timer) })
</script>

<style scoped>
.time {
  display: inline-block;
  margin-left: 6px;
  padding: 2px 10px;
  background: rgba(246, 193, 119, 0.14);
  border: 1px solid rgba(246, 193, 119, 0.32);
  border-radius: 999px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: #f6c177;
}
</style>