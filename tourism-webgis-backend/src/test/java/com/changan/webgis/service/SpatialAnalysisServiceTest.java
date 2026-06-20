package com.changan.webgis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.changan.webgis.model.MeasureRequest;
import com.changan.webgis.repository.AttractionRepository;
import com.changan.webgis.repository.SpatialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SpatialAnalysisServiceTest {

    private final SpatialRepository spatialRepository = mock(SpatialRepository.class);
    private final SpatialAnalysisService service = new SpatialAnalysisService(
            spatialRepository, mock(AttractionRepository.class), new ObjectMapper());

    @Test
    void rejectsLineWithOnePoint() throws Exception {
        var geometry = new ObjectMapper().readTree("""
                {"type":"LineString","coordinates":[[108.94,34.26]]}
                """);
        assertThrows(IllegalArgumentException.class,
                () -> service.measure(new MeasureRequest(geometry)));
    }

    @Test
    void rejectsUnclosedPolygon() throws Exception {
        var geometry = new ObjectMapper().readTree("""
                {"type":"Polygon","coordinates":[[
                  [108.90,34.20],[109.00,34.20],[109.00,34.30],[108.90,34.30]
                ]]}
                """);
        assertThrows(IllegalArgumentException.class,
                () -> service.measure(new MeasureRequest(geometry)));
    }

    @Test
    void returnsLineSegmentsAndTotal() throws Exception {
        when(spatialRepository.segmentDistances(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(List.of(1000.0, 2000.0));
        when(spatialRepository.length(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(3000.0);
        var geometry = new ObjectMapper().readTree("""
                {"type":"LineString","coordinates":[
                  [108.90,34.20],[108.95,34.25],[109.00,34.30]
                ]}
                """);
        var result = service.measure(new MeasureRequest(geometry));
        assertEquals(3000.0, result.totalDistanceMeters());
        assertEquals(2, result.segmentDistancesMeters().size());
    }
}

