<template>
  <StatusPage
    :icon="icon"
    :glow-color="glowColor"
    :gradient-from="gradientFrom"
    :gradient-to="gradientTo"
    :title="title"
    :description="description"
    :actions="actions"
    :tips="tips"
  >
    <slot />
  </StatusPage>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import StatusPage from './StatusPage.vue'

const props = defineProps({
  // 文案(可按场景覆盖)
  icon:        { type: String, default: '📭' },
  title:       { type: String, default: '暂无内容' },
  description: { type: String, default: '这里空空如也,试试别的吧～' },

  // 视觉配色,默认走紫色系(和数据/列表语义一致)
  glowColor:    { type: String, default: '#a18cd1' },
  gradientFrom: { type: String, default: '#a18cd1' },
  gradientTo:   { type: String, default: '#fbc2eb' },

  // 操作按钮(传入则覆盖默认)
  // [{ text, type: 'primary' | 'ghost', onClick }]
  customActions: { type: Array, default: () => [] },

  // 默认行为:"刷新"与"返回"按钮是否展示
  showRefresh: { type: Boolean, default: true },
  showBack:    { type: Boolean, default: true },

  // 默认按钮文案
  refreshText: { type: String, default: '刷新试试' },
  backText:    { type: String, default: '返回上一页' },
})

const router = useRouter()

function reload() { window.location.reload() }
function goBack() { router.back() }

const tips = ['换个关键词试试', '稍后再来看看', '检查筛选条件是否过严']

const actions = computed(() => {
  if (props.customActions.length) return props.customActions
  const list = []
  if (props.showRefresh) list.push({ text: props.refreshText, type: 'primary', onClick: reload })
  if (props.showBack)    list.push({ text: props.backText,    type: 'ghost',   onClick: goBack })
  return list
})
</script>