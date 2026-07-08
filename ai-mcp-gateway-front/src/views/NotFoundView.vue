<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="nf">
    <div class="nf-card card animate-scale-in">
      <span class="eyebrow">404 · Not Found</span>
      <h1>这片空间似乎被搬走了</h1>
      <p>你访问的页面已不存在,或者链接敲错了。回到控制台继续工作吧。</p>
      <button class="btn btn-primary" @click="router.push('/dashboard')">
        <el-icon><Back /></el-icon> 回到控制台
      </button>
    </div>
  </div>
</template>

<style scoped>
.nf {
  position: relative;
  min-height: 100dvh;
  display: grid;
  place-items: center;
  background: var(--bg-base);
  background-image: var(--bg-mesh);
  overflow: hidden;
  padding: 24px;
}
.nf-card {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 460px;
  padding: 40px 36px;
  border-radius: var(--radius-2xl);
  border: 1px solid var(--hairline);
  box-shadow: var(--shadow-card);
}
h1 {
  font-size: var(--fs-4xl);
  font-weight: var(--fw-bold);
  letter-spacing: var(--ls-tight);
  margin: 14px 0 10px;
  color: var(--text-strong);
}
p {
  color: var(--text-muted);
  font-size: var(--fs-base);
  line-height: var(--lh-base);
  margin-bottom: 22px;
}
</style>
