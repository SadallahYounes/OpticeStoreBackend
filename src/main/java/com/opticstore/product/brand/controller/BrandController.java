package com.opticstore.product.brand.controller;

import com.opticstore.product.brand.dto.BrandResponse;
import com.opticstore.product.brand.service.BrandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin
public class BrandController {

    private final BrandService service;

    public BrandController(BrandService service) {
        this.service = service;
    }

    @GetMapping
    public List<BrandResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public BrandResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }
}
