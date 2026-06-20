<script setup>
import { computed, reactive, watch } from "vue";

const props = defineProps({
  attraction: {
    type: Object,
    default: null
  },
  pickedCoordinate: {
    type: Object,
    default: null
  },
  saving: Boolean
});

const emit = defineEmits(["save", "delete", "close", "pick"]);

const emptyForm = () => ({
  name: "",
  category: "历史文化",
  district: "碑林区",
  rating: 5,
  longitude: 108.94,
  latitude: 34.26,
  address: "",
  ticket: "免费开放",
  openingHours: "全天开放",
  description: "",
  image: "/images/history.svg"
});

const form = reactive(emptyForm());
const isEditing = computed(() => Boolean(props.attraction?.id));

watch(
  () => props.attraction,
  (value) => Object.assign(form, value || emptyForm()),
  { immediate: true }
);

watch(
  () => props.pickedCoordinate,
  (value) => {
    if (value) {
      form.longitude = Number(value.longitude.toFixed(6));
      form.latitude = Number(value.latitude.toFixed(6));
    }
  }
);

watch(
  () => form.category,
  (category) => {
    const images = {
      历史文化: "/images/history.svg",
      博物馆: "/images/museum.svg",
      宗教古迹: "/images/temple.svg",
      自然风光: "/images/nature.svg",
      城市休闲: "/images/city.svg"
    };
    form.image = images[category] || form.image;
  }
);

function submit() {
  emit("save", { ...form, rating: Number(form.rating) });
}
</script>

<template>
  <aside class="manager-panel glass-panel">
    <div class="tool-panel-heading">
      <div>
        <p class="eyebrow">ATTRACTION EDITOR</p>
        <h2>{{ isEditing ? "编辑景点" : "新增景点" }}</h2>
      </div>
      <button type="button" @click="$emit('close')">×</button>
    </div>

    <form class="manager-form" @submit.prevent="submit">
      <label class="full">
        <span>景点名称</span>
        <input v-model.trim="form.name" required maxlength="100" />
      </label>

      <label>
        <span>景点类型</span>
        <select v-model="form.category">
          <option>历史文化</option><option>博物馆</option><option>宗教古迹</option>
          <option>自然风光</option><option>城市休闲</option>
        </select>
      </label>
      <label>
        <span>行政区</span>
        <input v-model.trim="form.district" required />
      </label>
      <label>
        <span>推荐等级</span>
        <select v-model.number="form.rating">
          <option v-for="rating in [5, 4, 3, 2, 1]" :key="rating" :value="rating">
            {{ rating }} 星
          </option>
        </select>
      </label>
      <label>
        <span>参考票价</span>
        <input v-model.trim="form.ticket" required />
      </label>

      <label class="full">
        <span>详细地址</span>
        <input v-model.trim="form.address" required />
      </label>
      <label class="full">
        <span>开放时间</span>
        <input v-model.trim="form.openingHours" required />
      </label>

      <div class="coordinate-editor full">
        <div>
          <label><span>经度</span><input v-model.number="form.longitude" type="number" step="0.000001" required /></label>
          <label><span>纬度</span><input v-model.number="form.latitude" type="number" step="0.000001" required /></label>
        </div>
        <button type="button" @click="$emit('pick')">⌖ 地图拾取</button>
      </div>

      <label class="full">
        <span>景点简介</span>
        <textarea v-model.trim="form.description" required maxlength="1000"></textarea>
      </label>

      <div class="manager-actions full">
        <button v-if="isEditing" class="danger-button" type="button" @click="$emit('delete', attraction)">
          删除
        </button>
        <button class="primary-button" type="submit" :disabled="saving">
          {{ saving ? "正在保存..." : isEditing ? "保存修改" : "创建景点" }}
        </button>
      </div>
    </form>
  </aside>
</template>

