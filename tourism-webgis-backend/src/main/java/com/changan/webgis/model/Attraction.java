package com.changan.webgis.model;

public record Attraction(
        long id,
        String name,
        String category,
        String district,
        int rating,
        double longitude,
        double latitude,
        String address,
        String ticket,
        String openingHours,
        String description,
        String image,
        Double distanceMeters
) {
}

