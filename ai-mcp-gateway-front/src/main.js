import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// vxe-table — 替代 el-table,用于所有数据列表
import VxeUI from 'vxe-pc-ui'
import 'vxe-pc-ui/es/style.css'
import VxeTable from 'vxe-table'
import 'vxe-table/es/style.css'

import App from './App.vue'
import router from './router'
import './styles/index.css'

import * as ElementPlusIcons from '@element-plus/icons-vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// vxe 必须先 use VxeUI 再 use VxeTable,且挂载在 element-plus 之后
// 包到 try/catch:如果某个版本对当前 Vue 不兼容,只让 vxe 表格降级,
// 不要让整个 app 因为一次性插件报错而白屏。
try {
  app.use(VxeUI)
} catch (err) {
  // eslint-disable-next-line no-console
  console.warn('[VxeUI] plugin not loaded:', err)
}
try {
  app.use(VxeTable)
} catch (err) {
  // eslint-disable-next-line no-console
  console.warn('[VxeTable] plugin not loaded:', err)
}

// Global error handler — 把页面级未捕获异常打到 console,
// 不再让一个组件 error 把整个 root unmount 掉导致白屏。
app.config.errorHandler = (err, instance, info) => {
  // eslint-disable-next-line no-console
  console.error('[Vue error]', info, err)
}

// Register all icons globally as <component :is="iconName" />
for (const [name, component] of Object.entries(ElementPlusIcons)) {
  app.component(name, component)
}

app.mount('#app')