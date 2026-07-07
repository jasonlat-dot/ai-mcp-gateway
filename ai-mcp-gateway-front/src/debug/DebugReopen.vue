<!--
  DebugReopen —— 关闭面板后的小齿轮
  点击恢复 DebugPanel 显示。
-->
<template>
  <button
    v-if="!showDebugPanel && debugActive"
    type="button"
    class="debug-reopen"
    title="打开状态页调试面板"
    @click="showDebugPanel = true"
  >🐞</button>
</template>

<script setup>
import { useDebug } from './useDebug.js'

const { showDebugPanel, debugActive } = useDebug()
</script>