package com.opticstore.product.glasses.controller;

import com.opticstore.product.glasses.dto.GlassesAdminResponse;
import com.opticstore.product.glasses.dto.GlassesResponse;
import com.opticstore.product.glasses.dto.GlassesUpdateRequest;
import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.product.glasses.repository.GlassesRepository;
import com.opticstore.product.glasses.service.GlassesService;
import com.opticstore.utils.ImageUrlMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/glasses")
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
        List<GlassesResponse> result;
        if (category != null) {
            result = service.getByCategory(category);
        } else if (brand != null) {
            result = service.getByBrand(brand);
        } else {
            result = List.of();
        }

        return result;
    }

    // Add this new endpoint for single product
    @GetMapping("/{id}")
    public GlassesResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }
}
