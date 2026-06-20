import { onBeforeUnmount, shallowRef } from "vue";
import {
  Cartesian2,
  Cartesian3,
  Cartographic,
  Color,
  CustomDataSource,
  CallbackProperty,
  EllipsoidTerrainProvider,
  HeightReference,
  HorizontalOrigin,
  LabelStyle,
  Math as CesiumMath,
  NearFarScalar,
  OpenStreetMapImageryProvider,
  PolygonHierarchy,
  ScreenSpaceEventHandler,
  ScreenSpaceEventType,
  SceneMode,
  SceneTransforms,
  UrlTemplateImageryProvider,
  VerticalOrigin,
  Viewer
} from "cesium";
import { categoryColors } from "../data/attractions";

const XIAN_VIEW = {
  longitude: 108.955,
  latitude: 34.23,
  height: 95000
};

const basemapFactories = {
  osm: () =>
    new OpenStreetMapImageryProvider({
      url: "https://tile.openstreetmap.org/",
      credit: "© OpenStreetMap contributors"
    }),
  light: () =>
    new UrlTemplateImageryProvider({
      url: "https://basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png",
      credit: "© OpenStreetMap contributors © CARTO"
    }),
  imagery: () =>
    new UrlTemplateImageryProvider({
      url: "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
      credit: "Tiles © Esri"
    })
};

export function useCesiumMap({
  onAttractionClick,
  onCoordinateChange,
  onMapClick,
  getInteractionMode
}) {
  const viewer = shallowRef(null);
  const attractionSource = new CustomDataSource("attractions");
  const analysisSource = new CustomDataSource("analysis");
  const toolAnalysisSource = new CustomDataSource("tool-analysis");
  const entityById = new Map();
  let clickHandler;
  let mouseMoveHandler;
  let postRenderRemove;
  let selectedEntity;
  let popupUpdater = null;

  function init(container) {
    viewer.value = new Viewer(container, {
      animation: false,
      baseLayerPicker: false,
      fullscreenButton: false,
      geocoder: false,
      homeButton: false,
      infoBox: false,
      navigationHelpButton: false,
      sceneModePicker: false,
      selectionIndicator: false,
      timeline: false,
      terrainProvider: new EllipsoidTerrainProvider(),
      sceneMode: SceneMode.SCENE3D,
      baseLayer: false,
      requestRenderMode: true,
      maximumRenderTimeChange: Number.POSITIVE_INFINITY
    });

    viewer.value.scene.globe.baseColor = Color.fromCssColorString("#07131f");
    viewer.value.scene.globe.enableLighting = false;
    viewer.value.scene.skyAtmosphere.show = true;
    viewer.value.scene.fog.enabled = true;
    viewer.value.scene.screenSpaceCameraController.minimumZoomDistance = 300;
    viewer.value.scene.screenSpaceCameraController.maximumZoomDistance = 12000000;
    viewer.value.cesiumWidget.creditContainer.style.opacity = "0.72";
    viewer.value.dataSources.add(attractionSource);
    viewer.value.dataSources.add(analysisSource);
    viewer.value.dataSources.add(toolAnalysisSource);

    setBasemap("osm");
    setupInteractions();
    flyHome(0);
    return viewer.value;
  }

  function setBasemap(id) {
    if (!viewer.value || !basemapFactories[id]) return;
    const layers = viewer.value.imageryLayers;
    layers.removeAll();
    const layer = layers.addImageryProvider(basemapFactories[id]());
    layer.brightness = id === "imagery" ? 0.78 : 0.9;
    layer.contrast = id === "imagery" ? 1.1 : 1;
    viewer.value.scene.requestRender();
  }

  function renderAttractions(items) {
    attractionSource.entities.removeAll();
    entityById.clear();

    for (const attraction of items) {
      const color = Color.fromCssColorString(categoryColors[attraction.category]);
      const entity = attractionSource.entities.add({
        id: `attraction-${attraction.id}`,
        position: Cartesian3.fromDegrees(attraction.longitude, attraction.latitude, 16),
        point: {
          pixelSize: 13,
          color,
          outlineColor: Color.WHITE.withAlpha(0.92),
          outlineWidth: 2,
          heightReference: HeightReference.NONE,
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
          scaleByDistance: new NearFarScalar(1500, 1.25, 250000, 0.55)
        },
        label: {
          text: attraction.name,
          font: "500 14px 'Microsoft YaHei', sans-serif",
          fillColor: Color.WHITE,
          outlineColor: Color.fromCssColorString("#07131f"),
          outlineWidth: 4,
          style: LabelStyle.FILL_AND_OUTLINE,
          showBackground: true,
          backgroundColor: Color.fromCssColorString("#07131f").withAlpha(0.76),
          backgroundPadding: new Cartesian2(9, 6),
          pixelOffset: new Cartesian2(0, -24),
          horizontalOrigin: HorizontalOrigin.CENTER,
          verticalOrigin: VerticalOrigin.BOTTOM,
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
          distanceDisplayCondition: undefined,
          translucencyByDistance: new NearFarScalar(2000, 1, 180000, 0)
        },
        properties: {
          attractionId: attraction.id
        }
      });
      entityById.set(attraction.id, entity);
    }

    const selectedId = selectedEntity?.properties?.attractionId?.getValue?.();
    if (selectedEntity && !entityById.has(Number(selectedId))) {
      selectedEntity = null;
    }
    viewer.value?.scene.requestRender();
  }

  function setSelected(attraction) {
    if (selectedEntity?.point) {
      selectedEntity.point.pixelSize = 13;
      selectedEntity.point.outlineWidth = 2;
    }

    selectedEntity = attraction ? entityById.get(attraction.id) : null;
    if (selectedEntity?.point) {
      selectedEntity.point.pixelSize = 19;
      selectedEntity.point.outlineWidth = 4;
    }
    viewer.value?.scene.requestRender();
  }

  function flyTo(attraction) {
    if (!viewer.value || !attraction) return;
    setSelected(attraction);
    viewer.value.camera.flyTo({
      destination: Cartesian3.fromDegrees(attraction.longitude, attraction.latitude - 0.012, 2800),
      orientation: {
        heading: CesiumMath.toRadians(0),
        pitch: CesiumMath.toRadians(-42),
        roll: 0
      },
      duration: 1.7
    });
  }

  function flyHome(duration = 1.5) {
    if (!viewer.value) return;
    viewer.value.camera.flyTo({
      destination: Cartesian3.fromDegrees(
        XIAN_VIEW.longitude,
        XIAN_VIEW.latitude,
        XIAN_VIEW.height
      ),
      orientation: {
        heading: CesiumMath.toRadians(0),
        pitch: CesiumMath.toRadians(-88),
        roll: 0
      },
      duration
    });
  }

  function registerPopupUpdater(updater) {
    popupUpdater = updater;
  }

  function setupInteractions() {
    clickHandler = new ScreenSpaceEventHandler(viewer.value.scene.canvas);
    clickHandler.setInputAction((movement) => {
      const picked = viewer.value.scene.pick(movement.position);
      const id = picked?.id?.properties?.attractionId?.getValue?.();
      const mode = getInteractionMode?.() || "browse";
      if (id && mode === "browse") {
        onAttractionClick?.(Number(id));
        return;
      }
      if (id && (mode === "analysis-buffer" || mode === "analysis-route")) {
        onAttractionClick?.(Number(id));
        return;
      }
      if (mode !== "browse") {
        const cartesian = viewer.value.camera.pickEllipsoid(
          movement.position,
          viewer.value.scene.globe.ellipsoid
        );
        if (!cartesian) return;
        const cartographic = Cartographic.fromCartesian(cartesian);
        onMapClick?.({
          longitude: CesiumMath.toDegrees(cartographic.longitude),
          latitude: CesiumMath.toDegrees(cartographic.latitude)
        });
      }
    }, ScreenSpaceEventType.LEFT_CLICK);

    mouseMoveHandler = new ScreenSpaceEventHandler(viewer.value.scene.canvas);
    mouseMoveHandler.setInputAction((movement) => {
      const cartesian = viewer.value.camera.pickEllipsoid(
        movement.endPosition,
        viewer.value.scene.globe.ellipsoid
      );
      if (!cartesian) return;
      const cartographic = Cartographic.fromCartesian(cartesian);
      onCoordinateChange?.({
        longitude: CesiumMath.toDegrees(cartographic.longitude),
        latitude: CesiumMath.toDegrees(cartographic.latitude)
      });
    }, ScreenSpaceEventType.MOUSE_MOVE);

    postRenderRemove = viewer.value.scene.postRender.addEventListener(() => {
      if (!selectedEntity || !popupUpdater) return;
      const position = selectedEntity.position.getValue(viewer.value.clock.currentTime);
      const windowPosition = SceneTransforms.worldToWindowCoordinates(
        viewer.value.scene,
        position
      );
      popupUpdater(windowPosition || null);
    });
  }

  function showNearbyArea(center, radius) {
    analysisSource.entities.removeAll();
    analysisSource.entities.add({
      position: Cartesian3.fromDegrees(center.longitude, center.latitude),
      point: {
        pixelSize: 12,
        color: Color.fromCssColorString("#55d9ff"),
        outlineColor: Color.WHITE,
        outlineWidth: 2,
        disableDepthTestDistance: Number.POSITIVE_INFINITY
      },
      ellipse: {
        semiMajorAxis: radius,
        semiMinorAxis: radius,
        material: Color.fromCssColorString("#55d9ff").withAlpha(0.16),
        outline: true,
        outlineColor: Color.fromCssColorString("#55d9ff")
      }
    });
    viewer.value?.scene.requestRender();
  }

  function showPolygon(coordinates, dynamic = false) {
    analysisSource.entities.removeAll();
    if (!coordinates.length) return;
    const positions = () =>
      coordinates.map((coordinate) =>
        Cartesian3.fromDegrees(coordinate.longitude, coordinate.latitude)
      );
    for (const coordinate of coordinates) {
      analysisSource.entities.add({
        position: Cartesian3.fromDegrees(coordinate.longitude, coordinate.latitude),
        point: {
          pixelSize: 9,
          color: Color.fromCssColorString("#ffb45b"),
          outlineColor: Color.WHITE,
          outlineWidth: 2,
          disableDepthTestDistance: Number.POSITIVE_INFINITY
        }
      });
    }
    if (coordinates.length >= 2) {
      analysisSource.entities.add({
        polyline: {
          positions: dynamic ? new CallbackProperty(positions, false) : positions(),
          width: 3,
          material: Color.fromCssColorString("#ffb45b")
        }
      });
    }
    if (coordinates.length >= 3) {
      analysisSource.entities.add({
        polygon: {
          hierarchy: dynamic
            ? new CallbackProperty(() => new PolygonHierarchy(positions()), false)
            : new PolygonHierarchy(positions()),
          material: Color.fromCssColorString("#ffb45b").withAlpha(0.14),
          outline: true,
          outlineColor: Color.fromCssColorString("#ffb45b")
        }
      });
    }
    viewer.value?.scene.requestRender();
  }

  function clearAnalysis() {
    analysisSource.entities.removeAll();
    viewer.value?.scene.requestRender();
  }

  function showToolPoint(coordinate, color = "#55d9ff") {
    toolAnalysisSource.entities.removeAll();
    toolAnalysisSource.entities.add({
      position: Cartesian3.fromDegrees(coordinate.longitude, coordinate.latitude),
      point: {
        pixelSize: 13,
        color: Color.fromCssColorString(color),
        outlineColor: Color.WHITE,
        outlineWidth: 2,
        disableDepthTestDistance: Number.POSITIVE_INFINITY
      }
    });
    viewer.value?.scene.requestRender();
  }

  function showGeoJsonPolygon(geometry, color = "#55d9ff") {
    toolAnalysisSource.entities.removeAll();
    const ring = geometry?.coordinates?.[0] || [];
    if (ring.length < 4) return;
    const positions = ring.map(([longitude, latitude]) =>
      Cartesian3.fromDegrees(longitude, latitude)
    );
    toolAnalysisSource.entities.add({
      polygon: {
        hierarchy: new PolygonHierarchy(positions),
        material: Color.fromCssColorString(color).withAlpha(0.18),
        outline: true,
        outlineColor: Color.fromCssColorString(color)
      }
    });
    viewer.value?.scene.requestRender();
  }

  function showMeasurement(coordinates, type) {
    toolAnalysisSource.entities.removeAll();
    if (!coordinates.length) return;
    const positions = coordinates.map((coordinate) =>
      Cartesian3.fromDegrees(coordinate.longitude, coordinate.latitude)
    );
    coordinates.forEach((coordinate, index) => {
      toolAnalysisSource.entities.add({
        position: Cartesian3.fromDegrees(coordinate.longitude, coordinate.latitude),
        point: {
          pixelSize: 9,
          color: Color.fromCssColorString("#ffb45b"),
          outlineColor: Color.WHITE,
          outlineWidth: 2,
          disableDepthTestDistance: Number.POSITIVE_INFINITY
        },
        label: {
          text: String(index + 1),
          font: "600 12px sans-serif",
          fillColor: Color.WHITE,
          outlineColor: Color.fromCssColorString("#07131f"),
          outlineWidth: 3,
          style: LabelStyle.FILL_AND_OUTLINE,
          pixelOffset: new Cartesian2(0, -20),
          disableDepthTestDistance: Number.POSITIVE_INFINITY
        }
      });
    });
    if (positions.length >= 2) {
      toolAnalysisSource.entities.add({
        polyline: {
          positions,
          width: 4,
          material: Color.fromCssColorString("#ffb45b")
        }
      });
    }
    if (type === "area" && positions.length >= 3) {
      toolAnalysisSource.entities.add({
        polygon: {
          hierarchy: new PolygonHierarchy(positions),
          material: Color.fromCssColorString("#ffb45b").withAlpha(0.16),
          outline: true,
          outlineColor: Color.fromCssColorString("#ffb45b")
        }
      });
    }
    viewer.value?.scene.requestRender();
  }

  function showRoute(attractions, geometry = null, routingMode = "PREVIEW") {
    toolAnalysisSource.entities.removeAll();
    if (!attractions.length) return;
    const stopPositions = attractions.map((attraction) =>
      Cartesian3.fromDegrees(attraction.longitude, attraction.latitude, 30)
    );
    const routeCoordinates = geometry?.coordinates;
    const routePositions = Array.isArray(routeCoordinates) && routeCoordinates.length >= 2
      ? routeCoordinates.map(([longitude, latitude]) =>
          Cartesian3.fromDegrees(longitude, latitude, 24)
        )
      : stopPositions;
    const routeColor = routingMode === "STRAIGHT_FALLBACK" ? "#ffb45b" : "#ff7ca8";
    toolAnalysisSource.entities.add({
      polyline: {
        positions: routePositions,
        width: 5,
        material: Color.fromCssColorString(routeColor)
      }
    });
    attractions.forEach((attraction, index) => {
      toolAnalysisSource.entities.add({
        position: stopPositions[index],
        point: {
          pixelSize: index === 0 ? 18 : 14,
          color: Color.fromCssColorString(index === 0 ? "#64e6a5" : "#ff7ca8"),
          outlineColor: Color.WHITE,
          outlineWidth: 3,
          disableDepthTestDistance: Number.POSITIVE_INFINITY
        },
        label: {
          text: index === 0 ? `起点 · ${attraction.name}` : `${index + 1} · ${attraction.name}`,
          font: "600 13px 'Microsoft YaHei', sans-serif",
          fillColor: Color.WHITE,
          outlineColor: Color.fromCssColorString("#07131f"),
          outlineWidth: 4,
          style: LabelStyle.FILL_AND_OUTLINE,
          showBackground: true,
          backgroundColor: Color.fromCssColorString("#07131f").withAlpha(0.82),
          pixelOffset: new Cartesian2(0, -26),
          disableDepthTestDistance: Number.POSITIVE_INFINITY
        }
      });
    });
    viewer.value?.scene.requestRender();
  }

  function clearToolAnalysis() {
    toolAnalysisSource.entities.removeAll();
    viewer.value?.scene.requestRender();
  }

  onBeforeUnmount(() => {
    clickHandler?.destroy();
    mouseMoveHandler?.destroy();
    postRenderRemove?.();
    viewer.value?.destroy();
    viewer.value = null;
  });

  return {
    viewer,
    init,
    setBasemap,
    renderAttractions,
    setSelected,
    flyTo,
    flyHome,
    registerPopupUpdater,
    showNearbyArea,
    showPolygon,
    clearAnalysis,
    showToolPoint,
    showGeoJsonPolygon,
    showMeasurement,
    showRoute,
    clearToolAnalysis
  };
}
