package com.changan.webgis.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record BufferRequest(
        @DecimalMin(value = "-180.0", message = "经度不能小于-180")
        @DecimalMax(value = "180.0", message = "经度不能大于180")
        double longitude,

        @DecimalMin(value = "-90.0", message = "纬度不能小于-90")
        @DecimalMax(value = "90.0", message = "纬度不能大于90")
        double latitude,

        @DecimalMin(value = "100.0", message = "缓冲半径不能小于100米")
        @DecimalMax(value = "50000.0", message = "缓冲半径不能大于50000米")
        double radius
) {
}

