package com.changan.webgis.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record MeasureRequest(
        @NotNull(message = "量算几何不能为空")
        JsonNode geometry
) {
}

