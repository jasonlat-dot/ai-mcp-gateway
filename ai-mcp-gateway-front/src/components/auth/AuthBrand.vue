<template>
  <section class="brand">
    <div class="brand-inner">
      <div class="brand-badge anim-up" style="--d:0ms">
        <span class="dot" />{{ badgeText }}
      </div>

      <h1 class="brand-title anim-up" style="--d:80ms">
        欢迎来到 <span class="grad-text">{{ productName }}</span>
      </h1>

      <p class="brand-slogan anim-up" style="--d:160ms">
        {{ sloganLine1 }}<br />
        {{ sloganLine2 }}
      </p>

      <div class="brand-divider anim-up" style="--d:240ms" />

      <!-- 4 个卡通角色 - 占据左侧 features 区域, 与右侧输入框水平对齐 -->
      <div class="brand-characters-wrap anim-up" style="--d:320ms">
        <slot name="characters" />
      </div>

      <div class="brand-footer anim-up" style="--d:400ms">
        © {{ year }} {{ copyright }}. All Rights Reserved.
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  badgeText:  { type: String,  default: '轻奢商务 · 一站式工作台' },
  productName:{ type: String,  default: 'Nexus Apex' },
  sloganLine1:{ type: String,  default: '以极简之力，构筑企业级协作体验。' },
  sloganLine2:{ type: String,  default: '安全 · 高效 · 优雅，一触即达。' },
  copyright:  { type: String,  default: 'Nexus Apex Inc.' },
  features: {
    type: Array,
    default: () => [
      { icon: '⚡', title: '极速接入',   desc: '全球加速节点，平均延迟 ≤ 60ms' },
      { icon: '🛡️', title: '金融级加密', desc: 'AES-256 + 国密 SM4 双算法守护' },
      { icon: '✨', title: '优雅交互',   desc: '每一处细节皆为沉浸体验而生' },
    ],
  },
})

const year = computed(() => new Date().getFullYear())
</script>

<style scoped>
.brand { color: #1e293b; }
.brand-inner { padding: 0 12px; }

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(186, 230, 253, 0.6);
  backdrop-filter: blur(8px);
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 1px;
  color: #0369a1;
  box-shadow: 0 4px 12px rgba(56, 189, 248, 0.14);
}
.brand-badge .dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #fbbf24; box-shadow: 0 0 8px #fcd34d;
}

.brand-title {
  margin: 14px 0 10px;
  font-size: 40px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 1px;
  color: #1e293b;
}
.grad-text {
  background: linear-gradient(120deg, #0ea5e9 0%, #f97316 50%, #f43f5e 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.brand-slogan {
  color: rgba(30, 41, 59, 0.7);
  font-size: 15px;
  line-height: 1.8;
}

.brand-divider {
  width: 64px; height: 3px;
  border-radius: 3px;
  margin: 18px 0 14px;
  background: linear-gradient(90deg, #7dd3fc, #fdba74, #fbcfe8);
  box-shadow: 0 0 12px rgba(125, 211, 252, 0.6);
}

/* 角色舞台 - 占据原本 features 区域, 高度与右侧 form 输入框区域对齐 */
.brand-characters-wrap {
  position: relative;
  margin: 0;
  width: 100%;
  height: 340px;
}

.brand-footer {
  margin-top: 14px;
  font-size: 12px;
  color: rgba(30, 41, 59, 0.45);
  letter-spacing: 1px;
}

.anim-up {
  opacity: 0;
  transform: translateY(18px);
  animation: fadeUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
  animation-delay: var(--d, 0ms);
}
@keyframes fadeUp {
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 960px) {
  .brand { text-align: center; }
  .brand-characters-wrap { display: none; }
  .brand-title { font-size: 32px; }
}
</style>
