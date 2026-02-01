package com.opticstore.product.glasses.controller;

import com.opticstore.product.glasses.dto.GlassesAdminResponse;
import com.opticstore.product.glasses.dto.GlassesResponse;
import com.opticstore.product.glasses.dto.GlassesUpdateRequest;
import com.opticstore.product.glasses.service.GlassesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/glasses")
public class GlassesController {

    private final GlassesService service;

    public GlassesController(GlassesService service) {
        this.service = service;
    }

    /*@GetMapping
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
    }*/

    // In your service or controller, add:
    @GetMapping
    public List<GlassesResponse> get(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand
    ) {
        System.out.println("🔍 Getting glasses...");

        List<GlassesResponse> result;
        if (category != null) {
            result = service.getByCategory(category);
        } else if (brand != null) {
            result = service.getByBrand(brand);
        } else {
            result = List.of();
        }

        // Debug the first item
        if (!result.isEmpty()) {
            GlassesResponse first = result.get(0);
            System.out.println("🔍 First glass in response:");
            System.out.println("  ID: " + first.id());
            System.out.println("  Name: " + first.name());
            System.out.println("  Image URL: " + first.imageUrl());
            System.out.println("  Has imageUrl: " + (first.imageUrl() != null));
        }

        return result;
    }

}
