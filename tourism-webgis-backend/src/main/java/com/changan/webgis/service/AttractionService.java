package com.changan.webgis.service;

import java.util.List;

import com.changan.webgis.exception.NotFoundException;
import com.changan.webgis.model.Attraction;
import com.changan.webgis.model.AttractionRequest;
import com.changan.webgis.model.StatisticsResponse;
import com.changan.webgis.repository.AttractionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttractionService {

    private final AttractionRepository repository;
    private final ObjectMapper objectMapper;

    public AttractionService(AttractionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public List<Attraction> findAll(String keyword, String category, String district, Integer rating) {
        validateRating(rating);
        return repository.findAll(keyword, category, district, rating);
    }

    public Attraction findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("未找到编号为 " + id + " 的景点"));
    }

    @Transactional
    public Attraction create(AttractionRequest request) {
        return repository.create(request);
    }

    @Transactional
    public Attraction update(long id, AttractionRequest request) {
        return repository.update(id, request)
                .orElseThrow(() -> new NotFoundException("未找到编号为 " + id + " 的景点"));
    }

    @Transactional
    public void delete(long id) {
        if (!repository.delete(id)) {
            throw new NotFoundException("未找到编号为 " + id + " 的景点");
        }
    }

    public List<Attraction> findNearby(double longitude, double latitude, double radius) {
        validateCoordinate(longitude, latitude);
        if (radius < 100 || radius > 50_000) {
            throw new IllegalArgumentException("查询半径必须在100米到50000米之间");
        }
        return repository.findNearby(longitude, latitude, radius);
    }

    public List<Attraction> findWithin(JsonNode geometry) {
        if (geometry == null || !"Polygon".equals(geometry.path("type").asText())) {
            throw new IllegalArgumentException("范围查询必须提交 GeoJSON Polygon");
        }
        JsonNode coordinates = geometry.path("coordinates");
        if (!coordinates.isArray() || coordinates.isEmpty() || coordinates.get(0).size() < 4) {
            throw new IllegalArgumentException("多边形至少需要3个顶点并闭合");
        }
        try {
            return repository.findWithin(objectMapper.writeValueAsString(geometry));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("GeoJSON 格式不正确");
        }
    }

    public StatisticsResponse statistics() {
        return new StatisticsResponse(
                repository.count(),
                repository.groupBy("category"),
                repository.groupBy("district"),
                repository.groupBy("rating")
        );
    }

    private void validateRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("推荐等级必须在1到5之间");
        }
    }

    private void validateCoordinate(double longitude, double latitude) {
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("经纬度超出有效范围");
        }
    }
}

