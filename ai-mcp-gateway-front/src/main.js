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
app.use(VxeUI)
app.use(VxeTable)

// Register all icons globally as <component :is="iconName" />
for (const [name, component] of Object.entries(ElementPlusIcons)) {
  app.component(name, component)
}

app.mount('#app')