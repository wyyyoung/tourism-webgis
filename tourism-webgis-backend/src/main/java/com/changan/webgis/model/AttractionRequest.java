package com.changan.webgis.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AttractionRequest(
        @NotBlank(message = "景点名称不能为空")
        @Size(max = 100, message = "景点名称不能超过100个字符")
        String name,

        @NotBlank(message = "景点类型不能为空")
        @Size(max = 30, message = "景点类型不能超过30个字符")
        String category,

        @NotBlank(message = "行政区不能为空")
        @Size(max = 30, message = "行政区不能超过30个字符")
        String district,

        @Min(value = 1, message = "推荐等级不能小于1")
        @Max(value = 5, message = "推荐等级不能大于5")
        int rating,

        @DecimalMin(value = "-180.0", message = "经度不能小于-180")
        @DecimalMax(value = "180.0", message = "经度不能大于180")
        double longitude,

        @DecimalMin(value = "-90.0", message = "纬度不能小于-90")
        @DecimalMax(value = "90.0", message = "纬度不能大于90")
        double latitude,

        @NotBlank(message = "地址不能为空")
        @Size(max = 200, message = "地址不能超过200个字符")
        String address,

        @NotBlank(message = "票价信息不能为空")
        @Size(max = 100, message = "票价信息不能超过100个字符")
        String ticket,

        @NotBlank(message = "开放时间不能为空")
        @Size(max = 100, message = "开放时间不能超过100个字符")
        String openingHours,

        @NotBlank(message = "景点简介不能为空")
        @Size(max = 1000, message = "景点简介不能超过1000个字符")
        String description,

        @NotBlank(message = "图片路径不能为空")
        @Size(max = 200, message = "图片路径不能超过200个字符")
        String image
) {
}

