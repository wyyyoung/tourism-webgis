package com.changan.webgis.model;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public record RouteResponse(
        List<Attraction> attractions,
        List<RouteSegment> segments,
        double totalDistanceMeters,
        Double totalDurationSeconds,
        JsonNode geometry,
        boolean optimized,
        RoutingMode routingMode,
        List<SnappedWaypoint> snappedWaypoints,
        String fallbackMessage
) {
    public record RouteSegment(
            long fromId,
            long toId,
            String fromName,
            String toName,
            double distanceMeters,
            Double durationSeconds
    ) {
    }

    public record SnappedWaypoint(
            long attractionId,
            String attractionName,
            double longitude,
            double latitude,
            double snapDistanceMeters
    ) {
    }

    public enum RoutingMode {
        DRIVING,
        STRAIGHT_FALLBACK
    }
}
