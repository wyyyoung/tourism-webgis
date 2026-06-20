package com.changan.webgis.model;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RouteRequest(
        @NotNull(message = "景点列表不能为空")
        @Size(min = 2, max = 8, message = "旅游线路必须包含2到8个景点")
        List<Long> attractionIds,
        boolean optimize
) {
}

