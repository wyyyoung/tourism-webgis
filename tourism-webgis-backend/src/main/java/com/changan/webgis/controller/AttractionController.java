package com.changan.webgis.controller;

import java.net.URI;
import java.util.List;

import com.changan.webgis.model.Attraction;
import com.changan.webgis.model.AttractionRequest;
import com.changan.webgis.service.AttractionService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    private final AttractionService service;

    public AttractionController(AttractionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Attraction> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer rating
    ) {
        return service.findAll(keyword, category, district, rating);
    }

    @GetMapping("/{id}")
    public Attraction findById(@PathVariable long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Attraction> create(@Valid @RequestBody AttractionRequest request) {
        Attraction attraction = service.create(request);
        return ResponseEntity.created(URI.create("/api/attractions/" + attraction.id()))
                .body(attraction);
    }

    @PutMapping("/{id}")
    public Attraction update(@PathVariable long id, @Valid @RequestBody AttractionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearby")
    public List<Attraction> nearby(
            @RequestParam double lng,
            @RequestParam double lat,
            @RequestParam double radius
    ) {
        return service.findNearby(lng, lat, radius);
    }

    @PostMapping("/within")
    public List<Attraction> within(@RequestBody JsonNode geometry) {
        return service.findWithin(geometry);
    }
}

