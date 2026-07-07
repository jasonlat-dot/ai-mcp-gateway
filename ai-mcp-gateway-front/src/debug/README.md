# `src/debug/` —— 状态页调试模块

> 正式开发时可整目录删除(连同 `main.js` 里的两行 import 和 `App.vue` 里的 4 个组件)。

## 目录结构

```
src/debug/
├── index.js            统一导出(barrel)
├── useDebug.js         composable:模块级共享 state + actions
├── statusRegistry.js   状态页注册表(添加/修改只动这里)
├── router.js           极简 vue-router 安装
├── styles.css          所有 .debug-* 样式
├── DebugLayer.vue      状态页预览层(占满视口)
├── DebugPanel.vue      右下浮动面板
├── DebugCorner.vue     左下角总开关
└── DebugReopen.vue     关闭面板后的小齿轮
```

## 怎么用

**默认行为** —— 模块加载即自动初始化,刷新页面即可看到浮动面板。

**总开关** —— 左下角"调试:开/关"按钮,关闭后整个 debug 模块隐藏,localStorage 持久化。

**URL 参数覆盖** —— `?debug=1` / `?debug=0` 比 localStorage 优先。

**直跳状态页** —— `http://localhost:5195/#/404` 等。

## 添加新状态页

只改 `statusRegistry.js`,加一行:

```js
{ path: '/xxx', label: 'xxx', icon: '🆕', comp: XxxPage, props: {} }
```

`props` 可以是对象,也可以是函数(函数会在每次进入时重新求值,适合 `endTime: () => Date.now() + 3600_000`)。

## 完全卸载调试模块

三步:

1. `main.js` 删除 `import { installDebugRouter } from './debug'` 和 `installDebugRouter(app).then(...)` 这一行
2. `App.vue` 删除 `import { DebugLayer, ... } from './debug'` 和模板里的 4 行 `<DebugLayer />` `<DebugPanel />` `<DebugCorner />` `<DebugReopen />`
3. 删除整个 `src/debug/` 目录

`main.js` 的 `import './styles/index.css'` 与 debug 无关,保留。
