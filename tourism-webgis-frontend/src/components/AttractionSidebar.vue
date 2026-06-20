<script setup>
import { computed } from "vue";
import { categoryColors } from "../data/attractions";

const props = defineProps({
  attractions: {
    type: Array,
    required: true
  },
  filters: {
    type: Object,
    required: true
  },
  categories: {
    type: Array,
    required: true
  },
  districts: {
    type: Array,
    required: true
  },
  selectedId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(["update-filter", "reset", "select", "fly"]);

const resultLabel = computed(() =>
  props.attractions.length ? `找到 ${props.attractions.length} 处景点` : "暂无匹配景点"
);

function updateFilter(key, event) {
  emit("update-filter", { key, value: event.target.value });
}
</script>

<template>
  <aside class="sidebar glass-panel">
    <div class="sidebar-heading">
      <div>
        <p class="eyebrow">ATTRACTION DISCOVERY</p>
        <h1>探索长安</h1>
      </div>
      <span class="result-count">{{ attractions.length }}</span>
    </div>

    <div class="filter-stack">
      <label class="search-field">
        <span aria-hidden="true">⌕</span>
        <input
          :value="filters.keyword"
          type="search"
          placeholder="搜索景点名称..."
          @input="updateFilter('keyword', $event)"
        />
      </label>

      <div class="select-grid">
        <label>
          <span>景点类型</span>
          <select :value="filters.category" @change="updateFilter('category', $event)">
            <option value="">全部类型</option>
            <option v-for="category in categories" :key="category" :value="category">
              {{ category }}
            </option>
          </select>
        </label>
        <label>
          <span>所在行政区</span>
          <select :value="filters.district" @change="updateFilter('district', $event)">
            <option value="">全部区域</option>
            <option v-for="district in districts" :key="district" :value="district">
              {{ district }}
            </option>
          </select>
        </label>
      </div>

      <div class="rating-filter">
        <span>推荐等级</span>
        <div class="rating-options">
          <button
            v-for="rating in [0, 5, 4, 3]"
            :key="rating"
            type="button"
            :class="{ active: Number(filters.rating) === rating }"
            @click="$emit('update-filter', { key: 'rating', value: rating })"
          >
            {{ rating === 0 ? "全部" : `${rating}星` }}
          </button>
        </div>
      </div>

      <div class="filter-summary">
        <span>{{ resultLabel }}</span>
        <button type="button" @click="$emit('reset')">重置筛选</button>
      </div>
    </div>

    <div class="attraction-list">
      <article
        v-for="item in attractions"
        :key="item.id"
        class="attraction-card"
        :class="{ selected: selectedId === item.id }"
        @click="$emit('select', item)"
      >
        <div
          class="card-index"
          :style="{ '--category-color': categoryColors[item.category] }"
        >
          {{ String(item.id).padStart(2, "0") }}
        </div>
        <div class="card-content">
          <div class="card-title-row">
            <h2>{{ item.name }}</h2>
            <span>{{ "★".repeat(item.rating) }}</span>
          </div>
          <div class="card-meta">
            <span>{{ item.category }}</span>
            <i></i>
            <span>{{ item.district }}</span>
          </div>
        </div>
        <button
          class="fly-button"
          type="button"
          title="飞行定位"
          @click.stop="$emit('fly', item)"
        >
          ➤
        </button>
      </article>

      <div v-if="!attractions.length" class="empty-state">
        <span>⌖</span>
        <strong>没有找到匹配景点</strong>
        <p>试试放宽筛选条件或重置筛选。</p>
      </div>
    </div>
  </aside>
</template>
