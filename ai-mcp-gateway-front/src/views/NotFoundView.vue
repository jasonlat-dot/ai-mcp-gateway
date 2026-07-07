<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="nf">
    <div class="nf-card">
      <span class="eyebrow">404 · Not Found</span>
      <h1>这片空间似乎被搬走了</h1>
      <p>你访问的页面已不存在,或者链接敲错了。回到控制台继续工作吧。</p>
      <button class="btn btn--primary" @click="router.push('/dashboard')">
        <el-icon><Back /></el-icon> 回到控制台
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.nf {
  position: relative;
  min-height: 100dvh;
  display: grid;
  place-items: center;
  background: var(--bg-base);
  overflow: hidden;
  padding: 24px;
}
.nf-card {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 460px;
  padding: 38px 32px;
  border-radius: var(--radius-xl);
  background: #ffffff;
  border: 1px solid var(--hairline-strong);
  box-shadow: var(--shadow-md);
}
h1 {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin: 14px 0 10px;
}
p {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 22px;
}
</style>