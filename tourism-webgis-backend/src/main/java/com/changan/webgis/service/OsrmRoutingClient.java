package com.changan.webgis.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.changan.webgis.config.RoutingProperties;
import com.changan.webgis.model.Attraction;
import com.changan.webgis.model.RouteResponse.SnappedWaypoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OsrmRoutingClient {

    private final HttpClient httpClient;
    private final RoutingProperties properties;
    private final ObjectMapper objectMapper;

    public OsrmRoutingClient(
            HttpClient routingHttpClient,
            RoutingProperties properties,
            ObjectMapper objectMapper
    ) {
        this.httpClient = routingHttpClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public DrivingMatrix table(List<Attraction> attractions) {
        JsonNode root = get("/table/v1/driving/" + coordinates(attractions)
                + "?annotations=distance,duration");
        JsonNode distancesNode = root.path("distances");
        JsonNode durationsNode = root.path("durations");
        int size = attractions.size();
        if (!distancesNode.isArray() || distancesNode.size() != size
                || !durationsNode.isArray() || durationsNode.size() != size) {
            throw new RoutingException("OSRM距离矩阵响应不完整");
        }
        double[][] distances = new double[size][size];
        double[][] durations = new double[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                JsonNode distance = distancesNode.get(row).get(column);
                JsonNode duration = durationsNode.get(row).get(column);
                if (distance == null || distance.isNull() || duration == null || duration.isNull()) {
                    throw new RoutingException("部分景点之间没有可用的驾车路线");
                }
                distances[row][column] = rounded(distance.asDouble());
                durations[row][column] = rounded(duration.asDouble());
            }
        }
        return new DrivingMatrix(distances, durations);
    }

    public DrivingRoute route(List<Attraction> attractions) {
        JsonNode root = get("/route/v1/driving/" + coordinates(attractions)
                + "?overview=full&geometries=geojson&steps=false");
        JsonNode routes = root.path("routes");
        if (!routes.isArray() || routes.isEmpty()) {
            throw new RoutingException("OSRM未返回可用驾车路线");
        }
        JsonNode route = routes.get(0);
        JsonNode geometry = route.path("geometry");
        if (!"LineString".equals(geometry.path("type").asText())) {
            throw new RoutingException("OSRM道路几何响应不正确");
        }
        JsonNode legs = route.path("legs");
        if (!legs.isArray() || legs.size() != attractions.size() - 1) {
            throw new RoutingException("OSRM道路分段响应不完整");
        }
        List<DrivingLeg> routeLegs = new ArrayList<>();
        for (JsonNode leg : legs) {
            routeLegs.add(new DrivingLeg(
                    rounded(leg.path("distance").asDouble()),
                    rounded(leg.path("duration").asDouble())));
        }
        List<SnappedWaypoint> waypoints = parseWaypoints(root.path("waypoints"), attractions);
        return new DrivingRoute(
                geometry.deepCopy(),
                rounded(route.path("distance").asDouble()),
                rounded(route.path("duration").asDouble()),
                routeLegs,
                waypoints
        );
    }

    private List<SnappedWaypoint> parseWaypoints(
            JsonNode waypointsNode,
            List<Attraction> attractions
    ) {
        if (!waypointsNode.isArray() || waypointsNode.size() != attractions.size()) {
            throw new RoutingException("OSRM道路吸附点响应不完整");
        }
        List<SnappedWaypoint> result = new ArrayList<>();
        for (int index = 0; index < attractions.size(); index++) {
            JsonNode waypoint = waypointsNode.get(index);
            JsonNode location = waypoint.path("location");
            if (!location.isArray() || location.size() < 2) {
                throw new RoutingException("OSRM道路吸附坐标缺失");
            }
            Attraction attraction = attractions.get(index);
            result.add(new SnappedWaypoint(
                    attraction.id(),
                    attraction.name(),
                    location.get(0).asDouble(),
                    location.get(1).asDouble(),
                    rounded(waypoint.path("distance").asDouble())
            ));
        }
        return result;
    }

    private JsonNode get(String path) {
        try {
            String baseUrl = properties.baseUrl().replaceAll("/+$", "");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(properties.readTimeoutSeconds()))
                    .header("Accept", "application/json")
                    .header("User-Agent", "XianTourismWebGIS/1.0 course-project")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RoutingException("OSRM服务返回HTTP " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            if (!"Ok".equals(root.path("code").asText())) {
                throw new RoutingException(
                        root.path("message").asText("OSRM未找到可用驾车路线"));
            }
            return root;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RoutingException("道路服务请求被中断", exception);
        } catch (IOException | IllegalArgumentException exception) {
            throw new RoutingException("无法连接OSRM道路服务", exception);
        }
    }

    private String coordinates(List<Attraction> attractions) {
        return attractions.stream()
                .map(attraction -> attraction.longitude() + "," + attraction.latitude())
                .reduce((first, second) -> first + ";" + second)
                .orElseThrow();
    }

    private double rounded(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public record DrivingMatrix(double[][] distances, double[][] durations) {
    }

    public record DrivingRoute(
            JsonNode geometry,
            double distanceMeters,
            double durationSeconds,
            List<DrivingLeg> legs,
            List<SnappedWaypoint> snappedWaypoints
    ) {
    }

    public record DrivingLeg(double distanceMeters, double durationSeconds) {
    }

    public static class RoutingException extends RuntimeException {
        public RoutingException(String message) {
            super(message);
        }

        public RoutingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

