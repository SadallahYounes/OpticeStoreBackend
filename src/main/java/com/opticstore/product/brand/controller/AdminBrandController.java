package com.opticstore.product.brand.controller;

import com.opticstore.product.brand.dto.BrandCreateRequest;
import com.opticstore.product.brand.dto.BrandResponse;
import com.opticstore.product.brand.dto.BrandUpdateRequest;
import com.opticstore.product.brand.service.BrandService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/brands")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBrandController {

    private final BrandService brandService;

    public AdminBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public BrandResponse create(@RequestBody BrandCreateRequest request) {
        return brandService.create(request);
    }

    @PutMapping("/{id}")
    public BrandResponse update(@PathVariable Long id, @RequestBody BrandUpdateRequest request) {
        return brandService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        brandService.delete(id);
    }
}