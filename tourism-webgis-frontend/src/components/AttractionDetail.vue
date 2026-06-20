<script setup>
defineProps({
  attraction: {
    type: Object,
    default: null
  }
});

defineEmits(["close", "fly"]);
</script>

<template>
  <Transition name="detail-slide">
    <aside v-if="attraction" class="detail-panel glass-panel">
      <button class="detail-close" type="button" aria-label="关闭详情" @click="$emit('close')">
        ×
      </button>

      <div class="detail-image">
        <img :src="attraction.image" :alt="`${attraction.name}占位图`" />
        <div class="image-overlay"></div>
        <div class="detail-image-copy">
          <span>{{ attraction.category }}</span>
          <h2>{{ attraction.name }}</h2>
          <p>{{ "★".repeat(attraction.rating) }}<i>{{ attraction.rating }}.0 推荐</i></p>
        </div>
      </div>

      <div class="detail-body">
        <p class="detail-description">{{ attraction.description }}</p>

        <dl>
          <div>
            <dt>所在区域</dt>
            <dd>{{ attraction.district }}</dd>
          </div>
          <div>
            <dt>详细地址</dt>
            <dd>{{ attraction.address }}</dd>
          </div>
          <div>
            <dt>开放时间</dt>
            <dd>{{ attraction.openingHours }}</dd>
          </div>
          <div>
            <dt>参考票价</dt>
            <dd>{{ attraction.ticket }}</dd>
          </div>
          <div>
            <dt>地理坐标</dt>
            <dd>{{ attraction.longitude.toFixed(4) }}, {{ attraction.latitude.toFixed(4) }}</dd>
          </div>
        </dl>

        <button class="primary-button" type="button" @click="$emit('fly', attraction)">
          <span aria-hidden="true">➤</span>
          飞行至景点
        </button>

        <p class="detail-note">开放时间和票价为课程演示数据，出行前请以景区官方公告为准。</p>
      </div>
    </aside>
  </Transition>
</template>
