<!--
  DebugPanel —— 浮动调试面板
  负责列出所有状态页入口 + 当前路径 + 使用提示。
  依赖 useDebug() 提供的共享状态。
-->
<template>
  <transition name="debug-fade">
    <div v-if="showDebugPanel" class="debug-floating">
      <div class="debug-header">
        <span class="debug-title">🐞 状态页调试</span>
        <div class="debug-header-actions">
          <button
            class="debug-mini"
            type="button"
            :title="debugPanelCollapsed ? '展开' : '收起'"
            @click="debugPanelCollapsed = !debugPanelCollapsed"
          >
            {{ debugPanelCollapsed ? '▴' : '▾' }}
          </button>
          <button
            class="debug-mini"
            type="button"
            title="关闭面板(仅隐藏,总开关可恢复)"
            @click="showDebugPanel = false"
          >×</button>
        </div>
      </div>

      <div v-show="!debugPanelCollapsed" class="debug-body">
        <div class="debug-hint">
          当前路径:<code>{{ currentHash || '/' }}</code>
        </div>

        <div class="debug-grid">
          <button
            v-for="p in statusPages"
            :key="p.path"
            type="button"
            class="debug-chip"
            :class="{ active: currentHash === p.path }"
            @click="goStatus(p.path)"
          >
            <span class="chip-icon">{{ p.icon }}</span>
            <span class="chip-label">{{ p.label }}</span>
          </button>
        </div>

        <div class="debug-actions">
          <button type="button" class="debug-link" @click="goStatus('')">↩ 回登录页</button>
        </div>

        <details class="debug-tips">
          <summary>使用方式</summary>
          <ul>
            <li>地址栏直接访问:<code>#/404</code> / <code>#/403</code> / <code>#/500</code> / <code>#/network</code> / <code>#/maintenance</code> / <code>#/empty</code> / <code>#/session</code></li>
            <li>也可点击上方胶囊按钮一键跳转</li>
            <li>正式开发:从 <code>main.js</code> 移除 <code>import './debug/router'</code> + 删除 <code>src/debug/</code> 目录即可</li>
          </ul>
        </details>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { statusPages } from './statusRegistry.js'
import { useDebug } from './useDebug.js'

const {
  showDebugPanel,
  debugPanelCollapsed,
  currentHash,
  goStatus,
} = useDebug()
</script>