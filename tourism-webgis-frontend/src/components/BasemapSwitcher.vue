<script setup>
defineProps({
  active: {
    type: String,
    required: true
  }
});

defineEmits(["change"]);

const basemaps = [
  { id: "osm", name: "街道", icon: "路" },
  { id: "light", name: "浅色", icon: "简" },
  { id: "imagery", name: "影像", icon: "星" }
];
</script>

<template>
  <div class="basemap-switcher glass-panel" aria-label="底图切换">
    <div class="tool-caption">底图</div>
    <button
      v-for="basemap in basemaps"
      :key="basemap.id"
      class="basemap-button"
      :class="{ active: active === basemap.id }"
      type="button"
      @click="$emit('change', basemap.id)"
    >
      <span>{{ basemap.icon }}</span>
      {{ basemap.name }}
    </button>
  </div>
</template>
