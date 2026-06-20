package com.changan.webgis.controller;

import com.changan.webgis.model.RouteRequest;
import com.changan.webgis.model.RouteResponse;
import com.changan.webgis.service.RoutePlanningService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RoutePlanningService service;

    public RouteController(RoutePlanningService service) {
        this.service = service;
    }

    @PostMapping("/calculate")
    public RouteResponse calculate(@Valid @RequestBody RouteRequest request) {
        return service.calculate(request);
    }
}

