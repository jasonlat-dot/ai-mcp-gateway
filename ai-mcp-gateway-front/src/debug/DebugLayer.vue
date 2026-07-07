<!--
  DebugLayer —— 状态页预览层

  当 hash 命中已注册的状态页时,渲染对应组件;
  否则不渲染任何内容(让外层继续展示登录页)。

  这是 App.vue 模板里 DEBUG 区段唯一需要关心的地方。
-->
<template>
  <div v-if="debugActive && debugComponent" class="status-debug-page theme-light">
    <component :is="debugComponent" v-bind="debugProps" />
  </div>
</template>

<script setup>
import { useDebug } from './useDebug.js'

const { debugActive, debugComponent, debugProps } = useDebug()
</script>