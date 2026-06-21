<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import AppHeader from "./components/AppHeader.vue";
import AnalysisToolsPanel from "./components/AnalysisToolsPanel.vue";
import AttractionDetail from "./components/AttractionDetail.vue";
import AttractionManager from "./components/AttractionManager.vue";
import AttractionSidebar from "./components/AttractionSidebar.vue";
import BasemapSwitcher from "./components/BasemapSwitcher.vue";
import SpatialQueryPanel from "./components/SpatialQueryPanel.vue";
import StatisticsPanel from "./components/StatisticsPanel.vue";
import WorkspaceTools from "./components/WorkspaceTools.vue";
import { useCesiumMap } from "./composables/useCesiumMap";
import { categoryColors } from "./data/attractions";
import { attractionApi } from "./services/api";

const mapContainer = ref(null);
const demoMode = import.meta.env.VITE_DEPLOY_MODE === "demo";
const allAttractions = ref([]);
const selectedAttraction = ref(null);
const activeBasemap = ref("osm");
const activeMode = ref("browse");
const loading = ref(true);
const actionLoading = ref(false);
const errorMessage = ref("");
const notice = ref("");
const coordinates = reactive({ longitude: 108.94, latitude: 34.26 });
const popupPosition = reactive({ x: 0, y: 0, visible: false });
const filters = reactive({ keyword: "", category: "", district: "", rating: 0 });
const queryResults = ref(null);
const nearbyCenter = ref(null);
const nearbyRadius = ref(3000);
const polygonVertices = ref([]);
const statistics = ref(null);
const managerTarget = ref(null);
const pickedCoordinate = ref(null);
const pickingCoordinate = ref(false);
const analysisTool = ref("buffer");
const bufferCenter = ref(null);
const bufferRadius = ref(1000);
const bufferResult = ref(null);
const measurePoints = ref([]);
const measureResult = ref(null);
const routeStops = ref([]);
const routeOriginalStops = ref([]);
const routeResult = ref(null);

const categories = computed(() => [...new Set(allAttractions.value.map((item) => item.category))]);
const districts = computed(() =>
  [...new Set(allAttractions.value.map((item) => item.district))].sort((a, b) =>
    a.localeCompare(b, "zh-CN")
  )
);
const sourceAttractions = computed(() => queryResults.value ?? allAttractions.value);
const filteredAttractions = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase();
  return sourceAttractions.value.filter((item) => {
    const keywordMatch = !keyword || item.name.toLowerCase().includes(keyword);
    const categoryMatch = !filters.category || item.category === filters.category;
    const districtMatch = !filters.district || item.district === filters.district;
    const ratingMatch = !Number(filters.rating) || item.rating === Number(filters.rating);
    return keywordMatch && categoryMatch && districtMatch && ratingMatch;
  });
});

const {
  init, setBasemap, renderAttractions, setSelected, flyTo, flyHome,
  registerPopupUpdater, showNearbyArea, showPolygon, clearAnalysis,
  showToolPoint, showGeoJsonPolygon, showMeasurement, showRoute, clearToolAnalysis
} = useCesiumMap({
  onAttractionClick: (id) => {
    const attraction = allAttractions.value.find((item) => item.id === id);
    if (attraction) selectAttraction(attraction);
  },
  onCoordinateChange: (value) => Object.assign(coordinates, value),
  getInteractionMode: () => {
    if (activeMode.value === "analysis") {
      return `analysis-${analysisTool.value}`;
    }
    if (activeMode.value === "manage") {
      return pickingCoordinate.value ? "manage-pick" : "browse";
    }
    return activeMode.value;
  },
  onMapClick: handleMapClick
});

onMounted(async () => {
  init(mapContainer.value);
  registerPopupUpdater((position) => {
    popupPosition.visible = Boolean(position && selectedAttraction.value);
    if (position) Object.assign(popupPosition, position);
  });
  await loadAttractions();
});

watch(filteredAttractions, (items) => {
  renderAttractions(items);
  if (selectedAttraction.value && !items.some((item) => item.id === selectedAttraction.value.id)) {
    closeDetail();
  }
});

watch(nearbyRadius, (radius) => {
  if (nearbyCenter.value) showNearbyArea(nearbyCenter.value, radius);
});

async function loadAttractions(preserveSelection = false) {
  loading.value = true;
  errorMessage.value = "";
  const selectedId = preserveSelection ? selectedAttraction.value?.id : null;
  try {
    if (demoMode) {
      const { attractions } = await import("./data/attractions");
      allAttractions.value = attractions;
    } else {
      allAttractions.value = await attractionApi.list();
    }
    renderAttractions(filteredAttractions.value);
    if (selectedId) {
      selectedAttraction.value =
        allAttractions.value.find((item) => item.id === selectedId) || null;
    }
  } catch (error) {
    errorMessage.value = `无法连接后端：${error.message}`;
  } finally {
    loading.value = false;
  }
}

function updateFilter({ key, value }) {
  filters[key] = value;
}

function resetFilters() {
  Object.assign(filters, { keyword: "", category: "", district: "", rating: 0 });
}

function selectAttraction(attraction) {
  if (activeMode.value === "analysis" && analysisTool.value === "buffer") {
    setBufferCenter(attraction);
    return;
  }
  if (activeMode.value === "analysis" && analysisTool.value === "route") {
    addRouteStop(attraction);
    return;
  }
  selectedAttraction.value = attraction;
  setSelected(attraction);
  popupPosition.visible = true;
  if (activeMode.value === "manage") {
    managerTarget.value = attraction;
    pickedCoordinate.value = null;
  }
}

function selectAndFly(attraction) {
  selectAttraction(attraction);
  flyTo(attraction);
}

function closeDetail() {
  selectedAttraction.value = null;
  popupPosition.visible = false;
  setSelected(null);
}

function changeBasemap(id) {
  activeBasemap.value = id;
  setBasemap(id);
}

async function changeMode(mode) {
  if (demoMode && mode !== "browse") {
    showToast("当前为 GitHub Pages 只读演示版，部署在线后端后可使用空间分析与管理功能");
    return;
  }
  const previousMode = activeMode.value;
  activeMode.value = mode;
  closeDetail();
  pickingCoordinate.value = false;
  if (!["nearby", "polygon"].includes(mode)) clearSpatialQuery();
  if (mode === "polygon") {
    polygonVertices.value = [];
    showPolygon([]);
  }
  if (mode === "statistics") await loadStatistics();
  if (mode === "manage") {
    managerTarget.value = null;
    pickedCoordinate.value = null;
  }
  if (previousMode === "analysis" || mode === "analysis") {
    clearAnalysisTool();
  }
}

function handleMapClick(coordinate) {
  if (activeMode.value === "nearby") {
    nearbyCenter.value = coordinate;
    showNearbyArea(coordinate, nearbyRadius.value);
  } else if (activeMode.value === "polygon") {
    polygonVertices.value = [...polygonVertices.value, coordinate];
    showPolygon(polygonVertices.value, true);
  } else if (activeMode.value === "manage" && pickingCoordinate.value) {
    pickedCoordinate.value = coordinate;
    pickingCoordinate.value = false;
    showToast("已拾取景点坐标");
  } else if (activeMode.value === "analysis") {
    if (analysisTool.value === "buffer") {
      setBufferCenter(coordinate);
    } else if (analysisTool.value === "distance" || analysisTool.value === "area") {
      measurePoints.value = [...measurePoints.value, coordinate];
      measureResult.value = null;
      showMeasurement(measurePoints.value, analysisTool.value);
    }
  }
}

function changeAnalysisTool(tool) {
  clearAnalysisTool();
  analysisTool.value = tool;
}

function setBufferCenter(value) {
  bufferCenter.value = {
    longitude: value.longitude,
    latitude: value.latitude,
    name: value.name || null
  };
  bufferResult.value = null;
  queryResults.value = null;
  showToolPoint(bufferCenter.value);
}

async function runBufferAnalysis() {
  if (!bufferCenter.value) return;
  actionLoading.value = true;
  try {
    bufferResult.value = await attractionApi.buffer({
      longitude: bufferCenter.value.longitude,
      latitude: bufferCenter.value.latitude,
      radius: bufferRadius.value
    });
    queryResults.value = bufferResult.value.attractions;
    showGeoJsonPolygon(bufferResult.value.geometry);
    showToast(`缓冲区内共有 ${bufferResult.value.count} 处景点`);
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    actionLoading.value = false;
  }
}

function undoMeasurePoint() {
  measurePoints.value = measurePoints.value.slice(0, -1);
  measureResult.value = null;
  showMeasurement(measurePoints.value, analysisTool.value);
}

async function finishMeasurement() {
  const minimum = analysisTool.value === "distance" ? 2 : 3;
  if (measurePoints.value.length < minimum) return;
  const coordinates = measurePoints.value.map((point) => [point.longitude, point.latitude]);
  const geometry = analysisTool.value === "distance"
    ? { type: "LineString", coordinates }
    : { type: "Polygon", coordinates: [[...coordinates, coordinates[0]]] };
  actionLoading.value = true;
  try {
    measureResult.value = await attractionApi.measure(geometry);
    showMeasurement(measurePoints.value, analysisTool.value);
    showToast(analysisTool.value === "distance" ? "距离量算完成" : "面积量算完成");
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    actionLoading.value = false;
  }
}

function addRouteStop(attraction) {
  if (routeStops.value.some((item) => item.id === attraction.id)) {
    showToast("该景点已经在线路中");
    return;
  }
  if (routeStops.value.length >= 8) {
    showToast("一条线路最多选择8个景点");
    return;
  }
  routeStops.value = [...routeStops.value, attraction];
  routeOriginalStops.value = [...routeStops.value];
  routeResult.value = null;
  showRoute(routeStops.value);
}

function removeRouteStop(id) {
  routeStops.value = routeStops.value.filter((item) => item.id !== id);
  routeOriginalStops.value = routeOriginalStops.value.filter((item) => item.id !== id);
  routeResult.value = null;
  showRoute(routeStops.value);
}

async function moveRouteStop(index, direction) {
  const target = index + direction;
  if (index === 0 || target <= 0 || target >= routeStops.value.length) return;
  const reordered = [...routeStops.value];
  [reordered[index], reordered[target]] = [reordered[target], reordered[index]];
  routeStops.value = reordered;
  await calculateRoute(false);
}

async function calculateRoute(optimize) {
  if (routeStops.value.length < 2) return;
  actionLoading.value = true;
  try {
    routeResult.value = await attractionApi.calculateRoute(
      routeStops.value.map((item) => item.id), optimize
    );
    routeStops.value = routeResult.value.attractions;
    showRoute(
      routeStops.value,
      routeResult.value.geometry,
      routeResult.value.routingMode
    );
    showToast(
      routeResult.value.routingMode === "DRIVING"
        ? optimize ? "已生成最短驾车路线" : "已生成当前顺序驾车路线"
        : "道路服务不可用，已回退为空间直线"
    );
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    actionLoading.value = false;
  }
}

async function restoreRouteSelection() {
  routeStops.value = [...routeOriginalStops.value];
  await calculateRoute(false);
}

function clearAnalysisTool() {
  bufferCenter.value = null;
  bufferResult.value = null;
  measurePoints.value = [];
  measureResult.value = null;
  routeStops.value = [];
  routeOriginalStops.value = [];
  routeResult.value = null;
  queryResults.value = null;
  clearToolAnalysis();
}

async function runNearbyQuery() {
  if (!nearbyCenter.value) return;
  actionLoading.value = true;
  try {
    queryResults.value = await attractionApi.nearby({
      ...nearbyCenter.value, radius: nearbyRadius.value
    });
    showToast(`查询到 ${queryResults.value.length} 处周边景点`);
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    actionLoading.value = false;
  }
}

async function runPolygonQuery() {
  if (polygonVertices.value.length < 3) return;
  const ring = polygonVertices.value.map((item) => [item.longitude, item.latitude]);
  ring.push(ring[0]);
  actionLoading.value = true;
  try {
    queryResults.value = await attractionApi.within({
      type: "Polygon", coordinates: [ring]
    });
    showPolygon(polygonVertices.value);
    showToast(`范围内共有 ${queryResults.value.length} 处景点`);
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    actionLoading.value = false;
  }
}

function clearSpatialQuery() {
  queryResults.value = null;
  nearbyCenter.value = null;
  polygonVertices.value = [];
  clearAnalysis();
}

async function loadStatistics() {
  actionLoading.value = true;
  try {
    statistics.value = await attractionApi.statistics();
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    actionLoading.value = false;
  }
}

async function saveAttraction(payload) {
  actionLoading.value = true;
  const editing = Boolean(managerTarget.value?.id);
  try {
    const saved = editing
      ? await attractionApi.update(managerTarget.value.id, payload)
      : await attractionApi.create(payload);
    managerTarget.value = saved;
    selectedAttraction.value = saved;
    await loadAttractions(true);
    showToast(editing ? "景点信息已保存" : "景点已创建");
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    actionLoading.value = false;
  }
}

async function deleteAttraction(attraction) {
  if (!window.confirm(`确定删除“${attraction.name}”吗？此操作不可撤销。`)) return;
  actionLoading.value = true;
  try {
    await attractionApi.remove(attraction.id);
    managerTarget.value = null;
    closeDetail();
    await loadAttractions();
    showToast("景点已删除");
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    actionLoading.value = false;
  }
}

function startCoordinatePick() {
  pickingCoordinate.value = true;
  showToast("请在地图上单击景点位置");
}

function showToast(message) {
  notice.value = message;
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => (notice.value = ""), 2600);
}
</script>

<template>
  <main class="app-shell">
    <div ref="mapContainer" class="cesium-map"></div>
    <div class="map-vignette"></div>
    <AppHeader :total="allAttractions.length" @home="flyHome()" />
    <WorkspaceTools :active="activeMode" :demo-mode="demoMode" @change="changeMode" />
    <AttractionSidebar
      :attractions="filteredAttractions"
      :filters="filters"
      :categories="categories"
      :districts="districts"
      :selected-id="selectedAttraction?.id"
      @update-filter="updateFilter"
      @reset="resetFilters"
      @select="selectAttraction"
      @fly="selectAndFly"
    />
    <BasemapSwitcher :active="activeBasemap" @change="changeBasemap" />
    <AttractionDetail
      v-if="activeMode !== 'manage'"
      :attraction="selectedAttraction"
      @close="closeDetail"
      @fly="selectAndFly"
    />
    <SpatialQueryPanel
      v-if="activeMode === 'nearby' || activeMode === 'polygon'"
      :mode="activeMode"
      :center="nearbyCenter"
      :radius="nearbyRadius"
      :vertex-count="polygonVertices.length"
      :result-count="queryResults?.length ?? null"
      :loading="actionLoading"
      @update-radius="nearbyRadius = $event"
      @search="runNearbyQuery"
      @finish="runPolygonQuery"
      @clear="clearSpatialQuery"
    />
    <AnalysisToolsPanel
      v-if="activeMode === 'analysis'"
      :tool="analysisTool"
      :loading="actionLoading"
      :buffer-center="bufferCenter"
      :buffer-radius="bufferRadius"
      :buffer-result="bufferResult"
      :measure-points="measurePoints"
      :measure-result="measureResult"
      :route-stops="routeStops"
      :route-result="routeResult"
      @change-tool="changeAnalysisTool"
      @update-radius="bufferRadius = $event"
      @run-buffer="runBufferAnalysis"
      @undo-measure="undoMeasurePoint"
      @finish-measure="finishMeasurement"
      @calculate-route="calculateRoute"
      @remove-stop="removeRouteStop"
      @move-stop="moveRouteStop"
      @restore-route="restoreRouteSelection"
      @clear="clearAnalysisTool"
      @close="changeMode('browse')"
    />
    <StatisticsPanel
      v-if="activeMode === 'statistics'"
      :statistics="statistics"
      :loading="actionLoading"
      @close="changeMode('browse')"
    />
    <AttractionManager
      v-if="activeMode === 'manage'"
      :attraction="managerTarget"
      :picked-coordinate="pickedCoordinate"
      :saving="actionLoading"
      @save="saveAttraction"
      @delete="deleteAttraction"
      @pick="startCoordinatePick"
      @close="changeMode('browse')"
    />

    <Transition name="popup-fade">
      <div
        v-if="selectedAttraction && popupPosition.visible && activeMode !== 'manage'"
        class="map-popup"
        :style="{
          left: `${popupPosition.x}px`,
          top: `${popupPosition.y}px`,
          '--category-color': categoryColors[selectedAttraction.category] || '#55d9ff'
        }"
      >
        <button type="button" aria-label="关闭地图弹窗" @click="closeDetail">×</button>
        <div class="popup-category">{{ selectedAttraction.category }}</div>
        <strong>{{ selectedAttraction.name }}</strong>
        <span>
          {{ selectedAttraction.district }} · {{ selectedAttraction.rating }} 星推荐
          <template v-if="selectedAttraction.distanceMeters">
            · {{ (selectedAttraction.distanceMeters / 1000).toFixed(2) }} km
          </template>
        </span>
        <i></i>
      </div>
    </Transition>

    <div v-if="loading" class="system-message loading-message">正在连接旅游资源数据库...</div>
    <div v-if="errorMessage" class="system-message error-message">
      <span>{{ errorMessage }}</span>
      <button type="button" @click="errorMessage = ''">×</button>
    </div>
    <Transition name="popup-fade">
      <div v-if="notice" class="toast-message">{{ notice }}</div>
    </Transition>
    <div v-if="pickingCoordinate" class="map-mode-hint">坐标拾取中 · 单击地图设置位置</div>
    <div v-if="demoMode" class="demo-mode-badge glass-panel">
      GitHub Pages 只读演示 · 其他功能需本地运行
    </div>

    <div class="coordinate-bar glass-panel">
      <span class="coordinate-pulse"></span>
      WGS84
      <strong>{{ coordinates.longitude.toFixed(5) }}°E</strong>
      <strong>{{ coordinates.latitude.toFixed(5) }}°N</strong>
    </div>
    <div class="map-legend glass-panel">
      <span v-for="(color, category) in categoryColors" :key="category">
        <i :style="{ background: color }"></i>{{ category }}
      </span>
    </div>
  </main>
</template>
