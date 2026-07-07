<template>
  <div class="bg-gradient" />
  <div class="bg-aurora">
    <span class="orb orb-1" />
    <span class="orb orb-2" />
    <span class="orb orb-3" />
  </div>
</template>

<script setup>
// 纯展示组件：底层柔和渐变 + 光晕流光。
// 与 ParticleBackground 解耦，可单独使用。
</script>

<style scoped>
.bg-gradient {
  position: fixed;
  inset: 0;
  z-index: 0;
  background:
    radial-gradient(1200px 800px at 10% 10%, rgba(186, 230, 253, 0.6) 0%, transparent 60%),
    radial-gradient(1000px 700px at 90% 90%, rgba(254, 215, 170, 0.6) 0%, transparent 60%),
    radial-gradient(900px 600px at 55% 50%, rgba(187, 247, 208, 0.45) 0%, transparent 70%),
    linear-gradient(135deg, #fffaf0 0%, #f0f9ff 50%, #fef3e8 100%);
}

.bg-aurora {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  /* 浅色背景下保持较低透明度,避免抢走卡片视觉焦点 */
  opacity: 0.5;
  /* 极慢节奏 28s,缩小位移,只作背景肌理 */
  animation: floatOrb 28s ease-in-out infinite;
}
.orb-1 { width: 480px; height: 480px; background: #7dd3fc; top: -120px; left: -120px; }
.orb-2 { width: 520px; height: 520px; background: #fdba74; bottom: -160px; right: -120px; animation-delay: -10s; }
.orb-3 { width: 360px; height: 360px; background: #86efac; top: 40%; left: 55%; animation-delay: -18s; opacity: 0.4; }

@keyframes floatOrb {
  0%, 100% { transform: translate(0, 0)        scale(1);    }
  50%      { transform: translate(30px, -20px)  scale(1.06); }
}
</style>
