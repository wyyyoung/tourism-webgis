package com.changan.webgis.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.changan.webgis.model.Attraction;
import com.changan.webgis.model.AttractionRequest;
import com.changan.webgis.model.StatisticsResponse.GroupCount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AttractionRepository {

    private static final String SELECT_FIELDS = """
            SELECT id, name, category, district, rating,
                   ST_X(location) AS longitude,
                   ST_Y(location) AS latitude,
                   address, ticket, opening_hours, description, image
            FROM tourist_attraction
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Attraction> attractionMapper = this::mapAttraction;

    public AttractionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Attraction> findAll(String keyword, String category, String district, Integer rating) {
        StringBuilder sql = new StringBuilder(SELECT_FIELDS).append(" WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND name ILIKE ?");
            parameters.add("%" + keyword.trim() + "%");
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            parameters.add(category);
        }
        if (district != null && !district.isBlank()) {
            sql.append(" AND district = ?");
            parameters.add(district);
        }
        if (rating != null) {
            sql.append(" AND rating = ?");
            parameters.add(rating);
        }
        sql.append(" ORDER BY rating DESC, id");

        return jdbcTemplate.query(sql.toString(), attractionMapper, parameters.toArray());
    }

    public Optional<Attraction> findById(long id) {
        return jdbcTemplate.query(SELECT_FIELDS + " WHERE id = ?", attractionMapper, id)
                .stream()
                .findFirst();
    }

    public Attraction create(AttractionRequest request) {
        Long id = jdbcTemplate.queryForObject("""
                INSERT INTO tourist_attraction (
                    name, category, district, rating, location,
                    address, ticket, opening_hours, description, image
                ) VALUES (?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?, ?, ?, ?, ?)
                RETURNING id
                """, Long.class,
                request.name(), request.category(), request.district(), request.rating(),
                request.longitude(), request.latitude(), request.address(), request.ticket(),
                request.openingHours(), request.description(), request.image());
        return findById(id).orElseThrow();
    }

    public Optional<Attraction> update(long id, AttractionRequest request) {
        int updated = jdbcTemplate.update("""
                UPDATE tourist_attraction
                SET name = ?, category = ?, district = ?, rating = ?,
                    location = ST_SetSRID(ST_MakePoint(?, ?), 4326),
                    address = ?, ticket = ?, opening_hours = ?,
                    description = ?, image = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                request.name(), request.category(), request.district(), request.rating(),
                request.longitude(), request.latitude(), request.address(), request.ticket(),
                request.openingHours(), request.description(), request.image(), id);
        return updated == 0 ? Optional.empty() : findById(id);
    }

    public boolean delete(long id) {
        return jdbcTemplate.update("DELETE FROM tourist_attraction WHERE id = ?", id) > 0;
    }

    public List<Attraction> findNearby(double longitude, double latitude, double radius) {
        return jdbcTemplate.query("""
                SELECT id, name, category, district, rating,
                       ST_X(location) AS longitude,
                       ST_Y(location) AS latitude,
                       address, ticket, opening_hours, description, image,
                       ST_Distance(
                           location::geography,
                           ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
                       ) AS distance_meters
                FROM tourist_attraction
                WHERE ST_DWithin(
                    location::geography,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                    ?
                )
                ORDER BY distance_meters
                """, attractionMapper, longitude, latitude, longitude, latitude, radius);
    }

    public List<Attraction> findWithin(String geoJson) {
        return jdbcTemplate.query("""
                SELECT id, name, category, district, rating,
                       ST_X(location) AS longitude,
                       ST_Y(location) AS latitude,
                       address, ticket, opening_hours, description, image
                FROM tourist_attraction
                WHERE ST_Covers(
                    ST_SetSRID(ST_GeomFromGeoJSON(?), 4326),
                    location
                )
                ORDER BY rating DESC, id
                """, attractionMapper, geoJson);
    }

    public long count() {
        Long value = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tourist_attraction", Long.class);
        return value == null ? 0 : value;
    }

    public List<GroupCount> groupBy(String column) {
        if (!List.of("category", "district", "rating").contains(column)) {
            throw new IllegalArgumentException("不支持的统计字段");
        }
        return jdbcTemplate.query(
                "SELECT " + column + "::text AS name, COUNT(*) AS value " +
                        "FROM tourist_attraction GROUP BY " + column + " ORDER BY value DESC, name",
                (resultSet, rowNum) ->
                        new GroupCount(resultSet.getString("name"), resultSet.getLong("value"))
        );
    }

    private Attraction mapAttraction(ResultSet resultSet, int rowNum) throws SQLException {
        Double distance = null;
        try {
            double value = resultSet.getDouble("distance_meters");
            if (!resultSet.wasNull()) {
                distance = Math.round(value * 10.0) / 10.0;
            }
        } catch (SQLException ignored) {
            // 普通查询不包含距离列。
        }

        return new Attraction(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getString("district"),
                resultSet.getInt("rating"),
                resultSet.getDouble("longitude"),
                resultSet.getDouble("latitude"),
                resultSet.getString("address"),
                resultSet.getString("ticket"),
                resultSet.getString("opening_hours"),
                resultSet.getString("description"),
                resultSet.getString("image"),
                distance
        );
    }
}

