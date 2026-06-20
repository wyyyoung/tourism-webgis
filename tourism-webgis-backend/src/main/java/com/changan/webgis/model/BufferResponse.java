package com.changan.webgis.model;

import java.util.List;

import com.changan.webgis.model.StatisticsResponse.GroupCount;
import com.fasterxml.jackson.databind.JsonNode;

public record BufferResponse(
        JsonNode geometry,
        List<Attraction> attractions,
        int count,
        List<GroupCount> categories
) {
}

