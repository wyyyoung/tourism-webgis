<script setup>
const props = defineProps({
  tool: { type: String, required: true },
  loading: Boolean,
  bufferCenter: { type: Object, default: null },
  bufferRadius: { type: Number, default: 1000 },
  bufferResult: { type: Object, default: null },
  measurePoints: { type: Array, default: () => [] },
  measureResult: { type: Object, default: null },
  routeStops: { type: Array, default: () => [] },
  routeResult: { type: Object, default: null }
});

defineEmits([
  "change-tool", "update-radius", "run-buffer", "undo-measure",
  "finish-measure", "clear", "calculate-route", "remove-stop",
  "move-stop", "restore-route", "close"
]);

const tools = [
  { id: "buffer", label: "缓冲区" },
  { id: "distance", label: "距离" },
  { id: "area", label: "面积" },
  { id: "route", label: "线路" }
];

function formatDistance(value) {
  if (value === null || value === undefined) return "—";
  return value >= 1000 ? `${(value / 1000).toFixed(2)} km` : `${value.toFixed(1)} m`;
}

function formatArea(value) {
  if (value === null || value === undefined) return "—";
  return value >= 1_000_000
    ? `${(value / 1_000_000).toFixed(2)} km²`
    : `${value.toFixed(1)} m²`;
}

function formatDuration(value) {
  if (value === null || value === undefined) return "—";
  const totalMinutes = Math.max(1, Math.round(value / 60));
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return hours ? `${hours}小时${minutes ? `${minutes}分钟` : ""}` : `${minutes}分钟`;
}
</script>

<template>
  <aside class="analysis-tools-panel glass-panel">
    <div class="tool-panel-heading">
      <div>
        <p class="eyebrow">SPATIAL TOOLBOX</p>
        <h2>空间分析工具</h2>
      </div>
      <button type="button" @click="$emit('close')">×</button>
    </div>

    <div class="analysis-tool-tabs">
      <button
        v-for="item in tools"
        :key="item.id"
        type="button"
        :class="{ active: tool === item.id }"
        @click="$emit('change-tool', item.id)"
      >
        {{ item.label }}
      </button>
    </div>

    <section v-if="tool === 'buffer'" class="analysis-section">
      <p class="tool-tip">点击景点或地图任意位置作为中心，由 PostGIS 生成真实缓冲区。</p>
      <div class="coordinate-readout">
        <span>缓冲中心</span>
        <strong v-if="bufferCenter">
          {{ bufferCenter.name || `${bufferCenter.longitude.toFixed(5)}, ${bufferCenter.latitude.toFixed(5)}` }}
        </strong>
        <strong v-else>尚未选择</strong>
      </div>
      <label class="range-field">
        <span>缓冲半径：{{ bufferRadius }} 米</span>
        <input
          :value="bufferRadius"
          type="range"
          min="100"
          max="50000"
          step="100"
          @input="$emit('update-radius', Number($event.target.value))"
        />
      </label>
      <button
        class="primary-button"
        type="button"
        :disabled="!bufferCenter || loading"
        @click="$emit('run-buffer')"
      >
        {{ loading ? "正在分析..." : "执行 ST_Buffer 分析" }}
      </button>
      <template v-if="bufferResult">
        <div class="analysis-metric">
          <span>缓冲区内景点</span><strong>{{ bufferResult.count }}</strong><i>处</i>
        </div>
        <div class="analysis-tags">
          <span v-for="item in bufferResult.categories" :key="item.name">
            {{ item.name }} {{ item.value }}
          </span>
        </div>
      </template>
    </section>

    <section v-else-if="tool === 'distance' || tool === 'area'" class="analysis-section">
      <p class="tool-tip">
        {{
          tool === "distance"
            ? "连续点击地图添加量算节点，至少2个点后完成。"
            : "连续点击地图绘制区域，至少3个点后完成。"
        }}
      </p>
      <div class="coordinate-readout">
        <span>已添加节点</span><strong>{{ measurePoints.length }} 个</strong>
      </div>
      <div class="inline-actions">
        <button type="button" :disabled="!measurePoints.length" @click="$emit('undo-measure')">
          撤销一点
        </button>
        <button
          type="button"
          :disabled="measurePoints.length < (tool === 'distance' ? 2 : 3) || loading"
          @click="$emit('finish-measure')"
        >
          {{ loading ? "计算中..." : "完成量算" }}
        </button>
      </div>
      <template v-if="measureResult">
        <div class="measure-summary">
          <div v-if="tool === 'distance'">
            <span>总距离</span><strong>{{ formatDistance(measureResult.totalDistanceMeters) }}</strong>
          </div>
          <template v-else>
            <div><span>面积</span><strong>{{ formatArea(measureResult.areaSquareMeters) }}</strong></div>
            <div><span>周长</span><strong>{{ formatDistance(measureResult.perimeterMeters) }}</strong></div>
          </template>
        </div>
        <ol class="segment-list">
          <li v-for="(distance, index) in measureResult.segmentDistancesMeters" :key="index">
            <span>第 {{ index + 1 }} 段</span><strong>{{ formatDistance(distance) }}</strong>
          </li>
        </ol>
      </template>
    </section>

    <section v-else class="analysis-section">
      <p class="tool-tip">从左侧列表或地图点击添加2–8个景点。首站固定，联网时按真实驾车道路规划。</p>
      <div v-if="!routeStops.length" class="route-empty">尚未选择线路景点</div>
      <ol v-else class="route-stop-list">
        <li v-for="(stop, index) in routeStops" :key="stop.id">
          <span class="route-number">{{ index === 0 ? "起" : index + 1 }}</span>
          <strong>{{ stop.name }}</strong>
          <div>
            <button type="button" :disabled="index <= 1" @click="$emit('move-stop', index, -1)">↑</button>
            <button
              type="button"
              :disabled="index === 0 || index === routeStops.length - 1"
              @click="$emit('move-stop', index, 1)"
            >↓</button>
            <button type="button" @click="$emit('remove-stop', stop.id)">×</button>
          </div>
        </li>
      </ol>
      <div class="route-actions">
        <button type="button" :disabled="routeStops.length < 2 || loading" @click="$emit('calculate-route', false)">
          按当前顺序
        </button>
        <button type="button" :disabled="routeStops.length < 2 || loading" @click="$emit('calculate-route', true)">
          自动优化
        </button>
      </div>
      <button
        v-if="routeResult"
        class="restore-button"
        type="button"
        @click="$emit('restore-route')"
      >
        恢复最初选择顺序
      </button>
      <template v-if="routeResult">
        <div
          class="routing-mode-badge"
          :class="{ fallback: routeResult.routingMode === 'STRAIGHT_FALLBACK' }"
        >
          {{ routeResult.routingMode === "DRIVING" ? "真实驾车路线" : "空间直线回退" }}
        </div>
        <div class="analysis-metric">
          <span>{{ routeResult.routingMode === "DRIVING" ? "总驾车里程" : "线路总距离" }}</span>
          <strong>{{ formatDistance(routeResult.totalDistanceMeters) }}</strong>
        </div>
        <div v-if="routeResult.totalDurationSeconds" class="route-duration">
          <span>预计驾驶时间</span>
          <strong>{{ formatDuration(routeResult.totalDurationSeconds) }}</strong>
        </div>
        <ol class="segment-list">
          <li v-for="segment in routeResult.segments" :key="`${segment.fromId}-${segment.toId}`">
            <span>{{ segment.fromName }} → {{ segment.toName }}</span>
            <strong>
              {{ formatDistance(segment.distanceMeters) }}
              <small v-if="segment.durationSeconds">{{ formatDuration(segment.durationSeconds) }}</small>
            </strong>
          </li>
        </ol>
        <p v-if="routeResult.fallbackMessage" class="route-fallback-message">
          {{ routeResult.fallbackMessage }}
        </p>
        <p class="route-disclaimer">
          {{
            routeResult.routingMode === "DRIVING"
              ? "驾车时间为OSRM估算，不包含实时交通拥堵。"
              : "当前为景点间空间直线，仅用于服务故障时保持功能可用。"
          }}
        </p>
      </template>
    </section>

    <button class="analysis-clear" type="button" @click="$emit('clear')">清除当前分析</button>
  </aside>
</template>
