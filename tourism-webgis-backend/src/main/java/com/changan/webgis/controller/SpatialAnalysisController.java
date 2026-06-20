package com.changan.webgis.controller;

import com.changan.webgis.model.BufferRequest;
import com.changan.webgis.model.BufferResponse;
import com.changan.webgis.model.MeasureRequest;
import com.changan.webgis.model.MeasureResponse;
import com.changan.webgis.service.SpatialAnalysisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spatial")
public class SpatialAnalysisController {

    private final SpatialAnalysisService service;

    public SpatialAnalysisController(SpatialAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/buffer")
    public BufferResponse buffer(@Valid @RequestBody BufferRequest request) {
        return service.buffer(request);
    }

    @PostMapping("/measure")
    public MeasureResponse measure(@Valid @RequestBody MeasureRequest request) {
        return service.measure(request);
    }
}

