package com.opticstore.product.glasses.controller;

import com.opticstore.product.brand.model.Brand;
import com.opticstore.product.brand.repository.BrandRepository;
import com.opticstore.product.category.model.Category;
import com.opticstore.product.category.repository.CategoryRepository;
import com.opticstore.product.glasses.dto.GlassesAdminResponse;
import com.opticstore.product.glasses.dto.GlassesCreateRequest;
import com.opticstore.product.glasses.dto.GlassesResponse;
import com.opticstore.product.glasses.dto.GlassesUpdateRequest;
import com.opticstore.product.glasses.service.GlassesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/glasses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGlassesController {

    private final GlassesService glassesService;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public AdminGlassesController(
            GlassesService glassesService,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository
    ) {
        this.glassesService = glassesService;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @PostMapping
    public GlassesResponse create(@RequestBody @Valid GlassesCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        return glassesService.create(request, category, brand);
    }

    @GetMapping
    public List<GlassesAdminResponse> getAll() {
        return glassesService.getAllForAdmin();
    }

    @PatchMapping("/{id}")
    public GlassesAdminResponse update(
            @PathVariable Long id,
            @RequestBody @Valid GlassesUpdateRequest request
    ) {
        return glassesService.updateGlass(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        glassesService.delete(id);  // This should work now!
    }
}

