<template>
  <StatusPage
    icon="📡"
    glow-color="#6ee2c1"
    gradient-from="#84fab0"
    gradient-to="#6ee2c1"
    title="网络连接失败"
    description="无法连接到服务器,请检查你的网络环境。"
    :actions="actions"
    :tips="tips"
  >
    <div class="diag">
      <div class="diag-row">
        <span class="dot" :class="online ? 'ok' : 'err'" />
        <span>浏览器在线:{{ online ? '正常' : '离线' }}</span>
      </div>
      <div class="diag-row" v-if="lastError">
        <span class="dot err" />
        <span>上次错误:{{ lastError }}</span>
      </div>
    </div>
  </StatusPage>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import StatusPage from './StatusPage.vue'

const props = defineProps({
  // 上一次失败的原因（catch 块里的 message 可传入）
  lastError: { type: String, default: '' },
})

const router = useRouter()
const online = ref(navigator.onLine)

function syncOnline()  { online.value = navigator.onLine }
function reload()      { window.location.reload() }
function goHome()      { router.push('/') }

const tips = ['检查 WiFi / 数据网络', '关闭代理或 VPN 重试', '联系网络管理员']

const actions = [
  { text: '重新加载', type: 'primary', onClick: reload },
  { text: '返回首页', type: 'ghost',   onClick: goHome },
]

onMounted(() => {
  window.addEventListener('online',  syncOnline)
  window.addEventListener('offline', syncOnline)
})
</script>

<style scoped>
.diag {
  display: inline-flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(186, 230, 253, 0.45);
  border-radius: 10px;
  font-size: 13px;
  color: rgba(30, 41, 59, 0.78);
  text-align: left;
}
.diag-row { display: flex; align-items: center; gap: 8px; }
.dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: rgba(30, 41, 59, 0.25);
}
.dot.ok  { background: #10b981; box-shadow: 0 0 8px #34d399; }
.dot.err { background: #f43f5e; box-shadow: 0 0 8px #fb7185; }
</style>