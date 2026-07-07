<template>
  <StatusPage
    icon="🔍"
    code="404"
    glow-color="#6a7bff"
    gradient-from="#8ec5fc"
    gradient-to="#e0c3fc"
    title="页面走丢了"
    description="你访问的页面可能已被搬走、回收或从未存在过。"
    :actions="actions"
    :tips="['检查地址栏拼写', '返回上一页', '通过搜索查找']"
  >
    <div>当前路径:<code class="path">{{ path }}</code></div>
  </StatusPage>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import StatusPage from './StatusPage.vue'

const router = useRouter()
const route  = useRoute()
const path   = computed(() => route.fullPath || '/')

function goHome()    { router.push('/') }
function goBack()    { router.back() }

const actions = [
  { text: '返回首页', type: 'primary', onClick: goHome },
  { text: '上一页',   type: 'ghost',   onClick: goBack },
]
</script>

<style scoped>
.path {
  display: inline-block;
  margin-left: 6px;
  padding: 2px 8px;
  background: rgba(186, 230, 253, 0.28);
  border: 1px solid rgba(186, 230, 253, 0.5);
  border-radius: 6px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: #0369a1;
}
</style>