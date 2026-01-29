package com.opticstore.product.glasses.controller;

import com.opticstore.product.glasses.dto.GlassesResponse;
import com.opticstore.product.glasses.service.GlassesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/glasses")
@CrossOrigin
public class GlassesController {

    private final GlassesService service;

    public GlassesController(GlassesService service) {
        this.service = service;
    }

    @GetMapping
    public List<GlassesResponse> get(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand
    ) {
        if (category != null) {
            return service.getByCategory(category);
        }
        if (brand != null) {
            return service.getByBrand(brand);
        }
        return List.of();
    }
}
