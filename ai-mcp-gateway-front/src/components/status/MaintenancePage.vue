<template>
  <StatusPage
    icon="🛠️"
    glow-color="#8ec5fc"
    gradient-from="#8ec5fc"
    gradient-to="#a18cd1"
    title="系统升级中"
    description="我们正在进行版本升级,期间服务将暂时不可用。"
    :actions="actions"
    :tips="tips"
  >
    <div v-if="endTime">预计恢复:<span class="time">{{ endTimeText }}</span></div>
    <div v-else-if="announce">公告:{{ announce }}</div>
  </StatusPage>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import StatusPage from './StatusPage.vue'

const props = defineProps({
  // 预计恢复时间(ISO 字符串 / 时间戳 / Date)
  endTime:  { type: [String, Number, Date], default: '' },
  // 自定义公告文案
  announce: { type: String, default: '' },
  // 状态页 URL(可选)
  statusUrl:{ type: String, default: '' },
})

const router = useRouter()
const now = ref(Date.now())
let timer = null

const endTs = computed(() => {
  if (!props.endTime) return 0
  const t = new Date(props.endTime).getTime()
  return isNaN(t) ? 0 : t
})

const endTimeText = computed(() => {
  if (!endTs.value) return ''
  const diff = Math.max(0, endTs.value - now.value)
  const h = Math.floor(diff / 3_600_000)
  const m = Math.floor((diff % 3_600_000) / 60_000)
  const s = Math.floor((diff % 60_000) / 1000)
  const pad = (n) => String(n).padStart(2, '0')
  const time = new Date(endTs.value).toLocaleTimeString('zh-CN', { hour12: false })
  return diff > 0 ? `${time}（剩余 ${pad(h)}:${pad(m)}:${pad(s)}）` : `已恢复(${time})`
})

const tips = computed(() => [
  '提前保存重要内容',
  '关注状态页获取通知',
  '升级完成后会自动刷新',
])

function refresh()  { window.location.reload() }
function goStatus() {
  if (props.statusUrl) window.open(props.statusUrl, '_blank')
  else router.push('/')
}

const actions = [
  { text: '刷新页面', type: 'primary', onClick: refresh },
  { text: '查看状态', type: 'ghost',   onClick: goStatus },
]

onMounted(() => {
  if (endTs.value) {
    timer = setInterval(() => { now.value = Date.now() }, 1000)
  }
})
onBeforeUnmount(() => { timer && clearInterval(timer) })
</script>

<style scoped>
.time {
  display: inline-block;
  margin-left: 6px;
  padding: 2px 10px;
  background: rgba(125, 211, 252, 0.35);
  border: 1px solid rgba(56, 189, 248, 0.55);
  border-radius: 999px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: #0369a1;
}
</style>