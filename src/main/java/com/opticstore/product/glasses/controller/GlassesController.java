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
        if (category != null && brand != null) {
            // Get by both category and brand (using brand NAME)
            result = service.getByCategoryAndBrand(category, brand);
        } else if (category != null) {
            result = service.getByCategory(category);
        } else if (brand != null) {
            result = service.getByBrand(brand);
        } else {
            result = service.getAllActive();
        }

        return result;
    }

    // Get by brand ID
    @GetMapping("/brand/{brandId}")
    public List<GlassesResponse> getByBrandId(@PathVariable Long brandId) {
        return service.getByBrandId(brandId);
    }

    @GetMapping("/brand/{brandId}/category/{category}")
    public List<GlassesResponse> getByBrandIdAndCategory(
            @PathVariable Long brandId,
            @PathVariable String category
    ) {
        return service.getByBrandIdAndCategory(brandId, category);
    }

    @GetMapping("/{id}")
    public GlassesResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }
}