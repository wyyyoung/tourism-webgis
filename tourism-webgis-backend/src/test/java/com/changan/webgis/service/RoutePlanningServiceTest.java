package com.changan.webgis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.changan.webgis.model.Attraction;
import com.changan.webgis.model.RouteRequest;
import com.changan.webgis.repository.AttractionRepository;
import com.changan.webgis.repository.SpatialRepository;
import com.changan.webgis.service.OsrmRoutingClient.DrivingLeg;
import com.changan.webgis.service.OsrmRoutingClient.DrivingMatrix;
import com.changan.webgis.service.OsrmRoutingClient.DrivingRoute;
import com.changan.webgis.service.OsrmRoutingClient.RoutingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class RoutePlanningServiceTest {

    private final AttractionRepository attractionRepository = mock(AttractionRepository.class);
    private final SpatialRepository spatialRepository = mock(SpatialRepository.class);
    private final OsrmRoutingClient osrmRoutingClient = mock(OsrmRoutingClient.class);
    private final RoutePlanningService service = new RoutePlanningService(
            attractionRepository, spatialRepository, osrmRoutingClient, new ObjectMapper());

    @Test
    void rejectsDuplicateAttractions() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(new RouteRequest(List.of(1L, 1L), true)));
    }

    @Test
    void keepsFirstAttractionAndOptimizesRemainingOrder() {
        Attraction first = attraction(1, "起点", 108.90);
        Attraction near = attraction(2, "近点", 108.91);
        Attraction far = attraction(3, "远点", 109.00);
        when(attractionRepository.findById(1)).thenReturn(Optional.of(first));
        when(attractionRepository.findById(2)).thenReturn(Optional.of(near));
        when(attractionRepository.findById(3)).thenReturn(Optional.of(far));
        when(spatialRepository.distance(108.90, 34.20, 108.91, 34.20)).thenReturn(1000.0);
        when(spatialRepository.distance(108.90, 34.20, 109.00, 34.20)).thenReturn(10000.0);
        when(spatialRepository.distance(109.00, 34.20, 108.91, 34.20)).thenReturn(9000.0);
        when(osrmRoutingClient.table(org.mockito.ArgumentMatchers.anyList()))
                .thenThrow(new RoutingException("测试回退"));

        var result = service.calculate(new RouteRequest(List.of(1L, 3L, 2L), true));

        assertEquals(List.of(1L, 2L, 3L),
                result.attractions().stream().map(Attraction::id).toList());
        assertEquals(10000.0, result.totalDistanceMeters());
        assertEquals("STRAIGHT_FALLBACK", result.routingMode().name());
    }

    @Test
    void usesDrivingMatrixAndRoadGeometry() throws Exception {
        Attraction first = attraction(1, "起点", 108.90);
        Attraction near = attraction(2, "近点", 108.91);
        Attraction far = attraction(3, "远点", 109.00);
        when(attractionRepository.findById(1)).thenReturn(Optional.of(first));
        when(attractionRepository.findById(2)).thenReturn(Optional.of(near));
        when(attractionRepository.findById(3)).thenReturn(Optional.of(far));
        when(osrmRoutingClient.table(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(new DrivingMatrix(
                        new double[][]{{0, 9000, 1000}, {9000, 0, 7000}, {1000, 7000, 0}},
                        new double[][]{{0, 900, 100}, {900, 0, 700}, {100, 700, 0}}
                ));
        var geometry = new ObjectMapper().readTree("""
                {"type":"LineString","coordinates":[[108.90,34.20],[108.91,34.20],[109.00,34.20]]}
                """);
        when(osrmRoutingClient.route(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(new DrivingRoute(
                        geometry, 8000, 800,
                        List.of(new DrivingLeg(1000, 100), new DrivingLeg(7000, 700)),
                        List.of()
                ));

        var result = service.calculate(new RouteRequest(List.of(1L, 3L, 2L), true));

        assertEquals(List.of(1L, 2L, 3L),
                result.attractions().stream().map(Attraction::id).toList());
        assertEquals("DRIVING", result.routingMode().name());
        assertEquals(800, result.totalDurationSeconds());
    }

    private Attraction attraction(long id, String name, double longitude) {
        return new Attraction(
                id, name, "历史文化", "碑林区", 5,
                longitude, 34.20, "地址", "免费", "全天", "简介",
                "/images/history.svg", null);
    }
}
