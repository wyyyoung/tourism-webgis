<script setup>
defineProps({
  statistics: {
    type: Object,
    default: null
  },
  loading: Boolean
});

defineEmits(["close"]);

function maxValue(items) {
  return Math.max(...(items || []).map((item) => item.value), 1);
}
</script>

<template>
  <aside class="statistics-panel glass-panel">
    <div class="tool-panel-heading">
      <div>
        <p class="eyebrow">RESOURCE INSIGHT</p>
        <h2>旅游资源统计</h2>
      </div>
      <button type="button" @click="$emit('close')">×</button>
    </div>

    <div v-if="loading" class="panel-loading">正在读取 PostGIS 统计数据...</div>
    <template v-else-if="statistics">
      <div class="statistics-total">
        <span>景点总数</span>
        <strong>{{ statistics.total }}</strong>
        <i>处</i>
      </div>

      <section>
        <h3>景点类型分布</h3>
        <div v-for="item in statistics.categories" :key="item.name" class="stat-row">
          <span>{{ item.name }}</span>
          <div><i :style="{ width: `${(item.value / maxValue(statistics.categories)) * 100}%` }"></i></div>
          <strong>{{ item.value }}</strong>
        </div>
      </section>

      <section>
        <h3>行政区资源数量</h3>
        <div class="district-chips">
          <span v-for="item in statistics.districts" :key="item.name">
            {{ item.name }}<strong>{{ item.value }}</strong>
          </span>
        </div>
      </section>

      <section>
        <h3>推荐等级</h3>
        <div class="rating-statistics">
          <div v-for="item in statistics.ratings" :key="item.name">
            <strong>{{ item.name }} 星</strong>
            <span>{{ item.value }} 处</span>
          </div>
        </div>
      </section>
    </template>
  </aside>
</template>

