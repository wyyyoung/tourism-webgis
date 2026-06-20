<script setup>
defineProps({
  active: {
    type: String,
    required: true
  },
  demoMode: {
    type: Boolean,
    default: false
  }
});

defineEmits(["change"]);

const tools = [
  { id: "browse", icon: "◎", label: "景点浏览" },
  { id: "nearby", icon: "◉", label: "周边查询" },
  { id: "polygon", icon: "◇", label: "范围查询" },
  { id: "analysis", icon: "⌁", label: "分析工具" },
  { id: "statistics", icon: "▥", label: "资源统计" },
  { id: "manage", icon: "✎", label: "景点管理" }
];
</script>

<template>
  <nav class="workspace-tools glass-panel" aria-label="功能模式">
    <button
      v-for="tool in tools"
      :key="tool.id"
      type="button"
      :class="{ active: active === tool.id }"
      :disabled="demoMode && tool.id !== 'browse'"
      :title="demoMode && tool.id !== 'browse' ? '在线只读演示版需部署后端后使用' : ''"
      @click="$emit('change', tool.id)"
    >
      <span>{{ tool.icon }}</span>
      {{ tool.label }}
    </button>
  </nav>
</template>
