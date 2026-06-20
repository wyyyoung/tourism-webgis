package com.changan.webgis.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SpatialRepository {

    private final JdbcTemplate jdbcTemplate;

    public SpatialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createBufferGeoJson(double longitude, double latitude, double radius) {
        return jdbcTemplate.queryForObject("""
                SELECT ST_AsGeoJSON(
                    ST_Buffer(
                        ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                        ?,
                        'quad_segs=24'
                    )::geometry
                )
                """, String.class, longitude, latitude, radius);
    }

    public double length(String geoJson) {
        Double value = jdbcTemplate.queryForObject("""
                SELECT ST_Length(
                    ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)::geography
                )
                """, Double.class, geoJson);
        return rounded(value);
    }

    public double perimeter(String geoJson) {
        Double value = jdbcTemplate.queryForObject("""
                SELECT ST_Perimeter(
                    ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)::geography
                )
                """, Double.class, geoJson);
        return rounded(value);
    }

    public double area(String geoJson) {
        Double value = jdbcTemplate.queryForObject("""
                SELECT ST_Area(
                    ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)::geography
                )
                """, Double.class, geoJson);
        return rounded(value);
    }

    public double distance(double fromLongitude, double fromLatitude,
                           double toLongitude, double toLatitude) {
        Double value = jdbcTemplate.queryForObject("""
                SELECT ST_Distance(
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
                )
                """, Double.class,
                fromLongitude, fromLatitude, toLongitude, toLatitude);
        return rounded(value);
    }

    public List<Double> segmentDistances(List<double[]> coordinates) {
        return java.util.stream.IntStream.range(0, coordinates.size() - 1)
                .mapToObj(index -> {
                    double[] from = coordinates.get(index);
                    double[] to = coordinates.get(index + 1);
                    return distance(from[0], from[1], to[0], to[1]);
                })
                .toList();
    }

    private double rounded(Double value) {
        if (value == null) {
            return 0;
        }
        return Math.round(value * 10.0) / 10.0;
    }
}

