package com.changan.webgis.model;

import java.util.List;

public record StatisticsResponse(
        long total,
        List<GroupCount> categories,
        List<GroupCount> districts,
        List<GroupCount> ratings
) {
    public record GroupCount(String name, long value) {
    }
}

