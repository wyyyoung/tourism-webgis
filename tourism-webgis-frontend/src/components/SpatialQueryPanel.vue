<script setup>
defineProps({
  mode: {
    type: String,
    required: true
  },
  center: {
    type: Object,
    default: null
  },
  radius: {
    type: Number,
    default: 3000
  },
  vertexCount: {
    type: Number,
    default: 0
  },
  resultCount: {
    type: Number,
    default: null
  },
  loading: Boolean
});

defineEmits(["update-radius", "search", "finish", "clear"]);
</script>

<template>
  <aside class="floating-tool-panel glass-panel">
    <div class="tool-panel-heading">
      <div>
        <p class="eyebrow">POSTGIS ANALYSIS</p>
        <h2>{{ mode === "nearby" ? "周边景点查询" : "多边形范围查询" }}</h2>
      </div>
      <button type="button" @click="$emit('clear')">清除</button>
    </div>

    <template v-if="mode === 'nearby'">
      <p class="tool-tip">在地图任意位置单击，设置查询中心。</p>
      <div class="coordinate-readout">
        <span>查询中心</span>
        <strong v-if="center">
          {{ center.longitude.toFixed(5) }}, {{ center.latitude.toFixed(5) }}
        </strong>
        <strong v-else>尚未选择</strong>
      </div>
      <label class="range-field">
        <span>查询半径：{{ radius }} 米</span>
        <input
          :value="radius"
          type="range"
          min="500"
          max="20000"
          step="500"
          @input="$emit('update-radius', Number($event.target.value))"
        />
      </label>
      <button class="primary-button" type="button" :disabled="!center || loading" @click="$emit('search')">
        {{ loading ? "正在查询..." : "执行 ST_DWithin 查询" }}
      </button>
    </template>

    <template v-else>
      <p class="tool-tip">依次单击地图添加顶点，至少选择3个顶点后完成查询。</p>
      <div class="coordinate-readout">
        <span>已选择顶点</span>
        <strong>{{ vertexCount }} 个</strong>
      </div>
      <button
        class="primary-button"
        type="button"
        :disabled="vertexCount < 3 || loading"
        @click="$emit('finish')"
      >
        {{ loading ? "正在查询..." : "闭合范围并查询" }}
      </button>
    </template>

    <div v-if="resultCount !== null" class="query-result">
      <span>空间查询结果</span>
      <strong>{{ resultCount }}</strong>
      <span>处景点</span>
    </div>
  </aside>
</template>

