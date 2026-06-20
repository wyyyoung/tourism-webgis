package com.changan.webgis.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.changan.webgis.model.Attraction;
import com.changan.webgis.model.BufferRequest;
import com.changan.webgis.model.BufferResponse;
import com.changan.webgis.model.MeasureRequest;
import com.changan.webgis.model.MeasureResponse;
import com.changan.webgis.model.StatisticsResponse.GroupCount;
import com.changan.webgis.repository.AttractionRepository;
import com.changan.webgis.repository.SpatialRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class SpatialAnalysisService {

    private final SpatialRepository spatialRepository;
    private final AttractionRepository attractionRepository;
    private final ObjectMapper objectMapper;

    public SpatialAnalysisService(
            SpatialRepository spatialRepository,
            AttractionRepository attractionRepository,
            ObjectMapper objectMapper
    ) {
        this.spatialRepository = spatialRepository;
        this.attractionRepository = attractionRepository;
        this.objectMapper = objectMapper;
    }

    public BufferResponse buffer(BufferRequest request) {
        List<Attraction> attractions = attractionRepository.findNearby(
                request.longitude(), request.latitude(), request.radius());
        Map<String, Long> counts = new LinkedHashMap<>();
        attractions.forEach(attraction ->
                counts.merge(attraction.category(), 1L, Long::sum));
        List<GroupCount> categories = counts.entrySet().stream()
                .map(entry -> new GroupCount(entry.getKey(), entry.getValue()))
                .toList();
        try {
            JsonNode geometry = objectMapper.readTree(spatialRepository.createBufferGeoJson(
                    request.longitude(), request.latitude(), request.radius()));
            return new BufferResponse(geometry, attractions, attractions.size(), categories);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("缓冲区结果无法转换为GeoJSON", exception);
        }
    }

    public MeasureResponse measure(MeasureRequest request) {
        JsonNode geometry = request.geometry();
        String type = geometry.path("type").asText();
        JsonNode coordinatesNode = geometry.path("coordinates");
        try {
            String geoJson = objectMapper.writeValueAsString(geometry);
            if ("LineString".equals(type)) {
                List<double[]> coordinates = parseLineCoordinates(coordinatesNode, 2);
                List<Double> segments = spatialRepository.segmentDistances(coordinates);
                return new MeasureResponse(
                        type, spatialRepository.length(geoJson), segments, null, null);
            }
            if ("Polygon".equals(type)) {
                if (!coordinatesNode.isArray() || coordinatesNode.isEmpty()) {
                    throw new IllegalArgumentException("多边形坐标不能为空");
                }
                List<double[]> ring = parseLineCoordinates(coordinatesNode.get(0), 4);
                ensureClosed(ring);
                double perimeter = spatialRepository.perimeter(geoJson);
                return new MeasureResponse(
                        type, perimeter, spatialRepository.segmentDistances(ring),
                        perimeter, spatialRepository.area(geoJson));
            }
            throw new IllegalArgumentException("量算仅支持LineString或Polygon");
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("GeoJSON格式不正确");
        }
    }

    private List<double[]> parseLineCoordinates(JsonNode coordinatesNode, int minimumSize) {
        if (!coordinatesNode.isArray() || coordinatesNode.size() < minimumSize) {
            throw new IllegalArgumentException(
                    minimumSize == 2 ? "距离量算至少需要2个点" : "面积量算至少需要3个点并闭合");
        }
        return java.util.stream.StreamSupport.stream(coordinatesNode.spliterator(), false)
                .map(this::parseCoordinate)
                .toList();
    }

    private double[] parseCoordinate(JsonNode coordinate) {
        if (!coordinate.isArray() || coordinate.size() < 2) {
            throw new IllegalArgumentException("坐标格式不正确");
        }
        double longitude = coordinate.get(0).asDouble();
        double latitude = coordinate.get(1).asDouble();
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("经纬度超出有效范围");
        }
        return new double[]{longitude, latitude};
    }

    private void ensureClosed(List<double[]> ring) {
        double[] first = ring.get(0);
        double[] last = ring.get(ring.size() - 1);
        if (first[0] != last[0] || first[1] != last[1]) {
            throw new IllegalArgumentException("面积量算多边形必须闭合");
        }
    }
}

