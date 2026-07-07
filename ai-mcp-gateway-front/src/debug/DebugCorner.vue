<!--
  DebugCorner —— 左下角调试总开关
  当总开关开启时显示"调试:开/关"按钮,点击切换整个 debug 模块。
-->
<template>
  <div v-if="debugActive" class="debug-corner">
    <button
      type="button"
      class="debug-toggle"
      :title="debugEnabled ? '关闭调试(整个 Debug 模块隐藏)' : '开启调试'"
      @click="toggleDebug"
    >
      {{ debugEnabled ? '调试:开' : '调试:关' }}
    </button>
  </div>
</template>

<script setup>
import { useDebug } from './useDebug.js'

const { debugActive, debugEnabled, toggleDebug } = useDebug()
</script>