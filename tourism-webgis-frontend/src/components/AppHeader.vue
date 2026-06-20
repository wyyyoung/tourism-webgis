<script setup>
defineProps({
  total: {
    type: Number,
    required: true
  }
});

defineEmits(["home"]);
</script>

<template>
  <header class="app-header glass-panel">
    <div class="brand">
      <div class="brand-mark" aria-hidden="true">
        <span></span>
        <span></span>
        <span></span>
      </div>
      <div>
        <div class="brand-title">长安迹</div>
        <div class="brand-subtitle">西安市旅游资源 WebGIS 平台</div>
      </div>
    </div>

    <div class="header-status">
      <span class="status-dot"></span>
      <span>已加载 {{ total }} 处旅游资源</span>
    </div>

    <button class="ghost-button header-home" type="button" @click="$emit('home')">
      <span aria-hidden="true">⌂</span>
      返回西安全景
    </button>
  </header>
</template>
