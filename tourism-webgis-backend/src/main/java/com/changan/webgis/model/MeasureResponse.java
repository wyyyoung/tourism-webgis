package com.changan.webgis.model;

import java.util.List;

public record MeasureResponse(
        String type,
        double totalDistanceMeters,
        List<Double> segmentDistancesMeters,
        Double perimeterMeters,
        Double areaSquareMeters
) {
}

