<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView v-slot="{ Component, route }">
    <transition name="route" mode="out-in" appear>
      <component :is="Component" :key="route.fullPath" />
    </transition>
  </RouterView>
</template>

<style>
.route-enter-from {
  opacity: 0;
  transform: translateY(12px) scale(0.995);
  filter: blur(6px);
}
.route-enter-active {
  transition:
    opacity 380ms cubic-bezier(0.32, 0.72, 0, 1),
    transform 480ms cubic-bezier(0.32, 0.72, 0, 1),
    filter 480ms cubic-bezier(0.32, 0.72, 0, 1);
}
.route-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
  filter: blur(0);
}
.route-leave-from { opacity: 1; }
.route-leave-active { transition: opacity 180ms ease; }
.route-leave-to { opacity: 0; }
</style>
