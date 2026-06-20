package com.changan.webgis.controller;

import com.changan.webgis.model.StatisticsResponse;
import com.changan.webgis.service.AttractionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final AttractionService service;

    public StatisticsController(AttractionService service) {
        this.service = service;
    }

    @GetMapping
    public StatisticsResponse statistics() {
        return service.statistics();
    }
}

