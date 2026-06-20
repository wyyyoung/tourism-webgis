package com.changan.webgis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "routing.osrm")
public record RoutingProperties(
        String baseUrl,
        int connectTimeoutSeconds,
        int readTimeoutSeconds
) {
}

