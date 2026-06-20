package com.changan.webgis.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.changan.webgis.repository.AttractionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AttractionServiceTest {

    private final AttractionService service =
            new AttractionService(mock(AttractionRepository.class), new ObjectMapper());

    @Test
    void rejectsInvalidNearbyRadius() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findNearby(108.94, 34.26, 50));
    }

    @Test
    void rejectsInvalidCoordinate() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findNearby(200, 34.26, 3000));
    }

    @Test
    void rejectsNonPolygonGeometry() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> service.findWithin(new ObjectMapper().readTree("""
                        {"type":"Point","coordinates":[108.94,34.26]}
                        """)));
    }
}

