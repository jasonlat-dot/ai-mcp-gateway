<template>
  <StatusPage
    icon="💥"
    code="500"
    glow-color="#ff7a8a"
    gradient-from="#ffb199"
    gradient-to="#ff7a8a"
    title="服务器开了个小差"
    description="服务暂时无法响应,我们的工程师正在赶往现场。"
    :actions="actions"
    :tips="['稍等几秒后重试', '问题持续可联系运维', '查看服务状态']"
  >
    <div v-if="traceId">错误追踪 ID:<code class="trace">{{ traceId }}</code></div>
  </StatusPage>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import StatusPage from './StatusPage.vue'

const props = defineProps({
  // 服务端下发的 traceId,方便工单定位
  traceId: { type: String, default: '' },
  // 自动重试秒数(0 表示不自动重试)
  retryAfter: { type: Number, default: 0 },
})

const router = useRouter()
const countdown = ref(props.retryAfter)
let timer = null

function reload() {
  if (props.retryAfter > 0) {
    countdown.value = props.retryAfter
    timer && clearInterval(timer)
    timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        clearInterval(timer)
        window.location.reload()
      }
    }, 1000)
  } else {
    window.location.reload()
  }
}

function goHome() { router.push('/') }

const actions = [
  { text: '刷新重试', type: 'primary', onClick: reload },
  { text: '返回首页', type: 'ghost',   onClick: goHome },
]

onMounted(() => { if (props.retryAfter > 0) reload() })
onBeforeUnmount(() => { timer && clearInterval(timer) })
</script>

<style scoped>
.trace {
  display: inline-block;
  margin-left: 6px;
  padding: 2px 8px;
  background: rgba(255, 122, 138, 0.12);
  border: 1px solid rgba(255, 122, 138, 0.3);
  border-radius: 6px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: #ffb199;
}
</style>