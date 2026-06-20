package com.changan.webgis.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.changan.webgis.exception.NotFoundException;
import com.changan.webgis.model.Attraction;
import com.changan.webgis.model.RouteRequest;
import com.changan.webgis.model.RouteResponse;
import com.changan.webgis.model.RouteResponse.RouteSegment;
import com.changan.webgis.model.RouteResponse.RoutingMode;
import com.changan.webgis.service.OsrmRoutingClient.DrivingMatrix;
import com.changan.webgis.service.OsrmRoutingClient.DrivingRoute;
import com.changan.webgis.service.OsrmRoutingClient.RoutingException;
import com.changan.webgis.repository.AttractionRepository;
import com.changan.webgis.repository.SpatialRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class RoutePlanningService {

    private final AttractionRepository attractionRepository;
    private final SpatialRepository spatialRepository;
    private final OsrmRoutingClient osrmRoutingClient;
    private final ObjectMapper objectMapper;

    public RoutePlanningService(
            AttractionRepository attractionRepository,
            SpatialRepository spatialRepository,
            OsrmRoutingClient osrmRoutingClient,
            ObjectMapper objectMapper
    ) {
        this.attractionRepository = attractionRepository;
        this.spatialRepository = spatialRepository;
        this.osrmRoutingClient = osrmRoutingClient;
        this.objectMapper = objectMapper;
    }

    public RouteResponse calculate(RouteRequest request) {
        List<Long> ids = request.attractionIds();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("旅游线路不能包含重复景点");
        }
        List<Attraction> attractions = ids.stream()
                .map(id -> attractionRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("未找到编号为 " + id + " 的景点")))
                .toList();

        try {
            return calculateDriving(attractions, request.optimize());
        } catch (RoutingException exception) {
            return calculateStraightFallback(attractions, request.optimize(), exception.getMessage());
        }
    }

    private RouteResponse calculateDriving(List<Attraction> attractions, boolean optimize) {
        List<Attraction> ordered = attractions;
        if (optimize) {
            DrivingMatrix matrix = osrmRoutingClient.table(attractions);
            ordered = optimizeWithMatrix(attractions, matrix.distances());
        }
        DrivingRoute route = osrmRoutingClient.route(ordered);
        List<RouteSegment> segments = new ArrayList<>();
        for (int index = 0; index < ordered.size() - 1; index++) {
            Attraction from = ordered.get(index);
            Attraction to = ordered.get(index + 1);
            var leg = route.legs().get(index);
            segments.add(new RouteSegment(
                    from.id(), to.id(), from.name(), to.name(),
                    leg.distanceMeters(), leg.durationSeconds()));
        }
        return new RouteResponse(
                ordered,
                segments,
                route.distanceMeters(),
                route.durationSeconds(),
                route.geometry(),
                optimize,
                RoutingMode.DRIVING,
                route.snappedWaypoints(),
                null
        );
    }

    private RouteResponse calculateStraightFallback(
            List<Attraction> attractions,
            boolean optimize,
            String reason
    ) {
        Map<String, Double> distanceCache = createDistanceCache(attractions);
        List<Attraction> ordered = optimize
                ? optimize(attractions, distanceCache)
                : attractions;
        List<RouteSegment> segments = createSegments(ordered, distanceCache);
        double total = rounded(segments.stream()
                .mapToDouble(RouteSegment::distanceMeters)
                .sum());
        return new RouteResponse(
                ordered,
                segments,
                total,
                null,
                createLineString(ordered),
                optimize,
                RoutingMode.STRAIGHT_FALLBACK,
                List.of(),
                "道路服务不可用，已回退为空间直线：" + reason
        );
    }

    private List<Attraction> optimizeWithMatrix(
            List<Attraction> attractions,
            double[][] distances
    ) {
        List<Integer> remaining = new ArrayList<>();
        for (int index = 1; index < attractions.size(); index++) {
            remaining.add(index);
        }
        MatrixBestRoute best = new MatrixBestRoute();
        permuteMatrix(0, remaining, new ArrayList<>(), 0, best, distances);
        List<Attraction> result = new ArrayList<>();
        result.add(attractions.get(0));
        best.order.forEach(index -> result.add(attractions.get(index)));
        return result;
    }

    private void permuteMatrix(
            int previous,
            List<Integer> remaining,
            List<Integer> current,
            double distance,
            MatrixBestRoute best,
            double[][] distances
    ) {
        if (distance >= best.distance) {
            return;
        }
        if (remaining.isEmpty()) {
            best.distance = distance;
            best.order = List.copyOf(current);
            return;
        }
        for (int index = 0; index < remaining.size(); index++) {
            int next = remaining.remove(index);
            current.add(next);
            permuteMatrix(next, remaining, current,
                    distance + distances[previous][next], best, distances);
            current.remove(current.size() - 1);
            remaining.add(index, next);
        }
    }

    private Map<String, Double> createDistanceCache(List<Attraction> attractions) {
        Map<String, Double> cache = new HashMap<>();
        for (int i = 0; i < attractions.size(); i++) {
            for (int j = i + 1; j < attractions.size(); j++) {
                Attraction first = attractions.get(i);
                Attraction second = attractions.get(j);
                cache.put(key(first.id(), second.id()), spatialRepository.distance(
                        first.longitude(), first.latitude(),
                        second.longitude(), second.latitude()));
            }
        }
        return cache;
    }

    private List<Attraction> optimize(
            List<Attraction> attractions,
            Map<String, Double> distanceCache
    ) {
        Attraction start = attractions.get(0);
        List<Attraction> remaining = new ArrayList<>(attractions.subList(1, attractions.size()));
        BestRoute best = new BestRoute();
        permute(start, remaining, new ArrayList<>(), 0, best, distanceCache);
        List<Attraction> result = new ArrayList<>();
        result.add(start);
        result.addAll(best.order);
        return result;
    }

    private void permute(
            Attraction previous,
            List<Attraction> remaining,
            List<Attraction> current,
            double distance,
            BestRoute best,
            Map<String, Double> distanceCache
    ) {
        if (distance >= best.distance) {
            return;
        }
        if (remaining.isEmpty()) {
            best.distance = distance;
            best.order = List.copyOf(current);
            return;
        }
        for (int index = 0; index < remaining.size(); index++) {
            Attraction next = remaining.remove(index);
            current.add(next);
            permute(next, remaining, current,
                    distance + distance(previous, next, distanceCache),
                    best, distanceCache);
            current.remove(current.size() - 1);
            remaining.add(index, next);
        }
    }

    private List<RouteSegment> createSegments(
            List<Attraction> attractions,
            Map<String, Double> distanceCache
    ) {
        List<RouteSegment> segments = new ArrayList<>();
        for (int index = 0; index < attractions.size() - 1; index++) {
            Attraction from = attractions.get(index);
            Attraction to = attractions.get(index + 1);
            segments.add(new RouteSegment(
                    from.id(), to.id(), from.name(), to.name(),
                    distance(from, to, distanceCache), null));
        }
        return segments;
    }

    private double distance(
            Attraction first,
            Attraction second,
            Map<String, Double> distanceCache
    ) {
        return distanceCache.get(key(first.id(), second.id()));
    }

    private String key(long first, long second) {
        return Math.min(first, second) + ":" + Math.max(first, second);
    }

    private JsonNode createLineString(List<Attraction> attractions) {
        var root = objectMapper.createObjectNode();
        root.put("type", "LineString");
        var coordinates = root.putArray("coordinates");
        attractions.forEach(attraction -> {
            var coordinate = coordinates.addArray();
            coordinate.add(attraction.longitude());
            coordinate.add(attraction.latitude());
        });
        return root;
    }

    private double rounded(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static class BestRoute {
        private double distance = Double.POSITIVE_INFINITY;
        private List<Attraction> order = List.of();
    }

    private static class MatrixBestRoute {
        private double distance = Double.POSITIVE_INFINITY;
        private List<Integer> order = List.of();
    }
}
