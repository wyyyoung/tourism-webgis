package com.changan.webgis.controller;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> health() {
        String postgisVersion = jdbcTemplate.queryForObject(
                "SELECT postgis_lib_version()", String.class);
        return Map.of(
                "status", "UP",
                "database", "PostgreSQL/PostGIS",
                "postgisVersion", postgisVersion
        );
    }
}

