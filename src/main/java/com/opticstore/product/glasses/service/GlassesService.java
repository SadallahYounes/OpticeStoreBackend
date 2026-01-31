package com.opticstore.product.glasses.service;

import com.opticstore.product.brand.model.Brand;
import com.opticstore.product.brand.repository.BrandRepository;
import com.opticstore.product.category.model.Category;
import com.opticstore.product.category.repository.CategoryRepository;
import com.opticstore.product.glasses.dto.GlassesAdminResponse;
import com.opticstore.product.glasses.dto.GlassesCreateRequest;
import com.opticstore.product.glasses.dto.GlassesResponse;
import com.opticstore.product.glasses.dto.GlassesUpdateRequest;
import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.product.glasses.repository.GlassesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlassesService {

    private final GlassesRepository glassesRepository;
    private final CategoryRepository categoryRepository ;
    private final BrandRepository brandRepository;


    public GlassesService(GlassesRepository glassesRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository) {
        this.glassesRepository = glassesRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    public List<GlassesResponse> getByCategory(String slug) {
        return glassesRepository.findByCategory_Slug(slug)
                .stream()
                .map(g -> new GlassesResponse(
                        g.getId(),
                        g.getName(),
                        g.getPrice(),
                        g.getImageUrl(),
                        g.getCategory().getName(),
                        g.getBrand().getName()
                ))
                .toList();
    }

    public List<GlassesResponse> getByBrand(String brand) {
        return glassesRepository.findByBrand_NameIgnoreCase(brand)
                .stream()
                .map(g -> new GlassesResponse(
                        g.getId(),
                        g.getName(),
                        g.getPrice(),
                        g.getImageUrl(),
                        g.getCategory().getName(),
                        g.getBrand().getName()
                ))
                .toList();
    }


    /* ================== ADMIN ================== */

    @Transactional
    public GlassesResponse create(GlassesCreateRequest request,
                                  Category category,
                                  Brand brand) {

        if (request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Glasses glasses = new Glasses();
        glasses.setName(request.getName());
        glasses.setPrice(request.getPrice());
        glasses.setImageUrl(request.getImageUrl());
        glasses.setQuantity(request.getQuantity());
        glasses.setCategory(category);
        glasses.setBrand(brand);

        Glasses saved = glassesRepository.save(glasses);

        return new GlassesResponse(
                saved.getId(),
                saved.getName(),
                saved.getPrice(),
                saved.getImageUrl(),
                saved.getCategory().getName(),
                saved.getBrand().getName()
        );
    }


    @Transactional
    public void updateStock(Long glassesId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Glasses glasses = glassesRepository.findById(glassesId)
                .orElseThrow(() -> new RuntimeException("Glasses not found"));

        glasses.setQuantity(newQuantity);
    }

    public List<GlassesAdminResponse> getAllForAdmin() {
        return glassesRepository.findAll()
                .stream()
                .map(g -> new GlassesAdminResponse(
                        g.getId(),
                        g.getName(),
                        g.getPrice(),
                        g.getQuantity(),
                        g.getCategory().getName(),
                        g.getBrand().getName(),
                        g.getImageUrl()
                ))
                .toList();
    }


    @Transactional
    public GlassesAdminResponse updateGlass(Long id, GlassesUpdateRequest req) {

        Glasses g = glassesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Glass not found"));

        if (req.name() != null) g.setName(req.name());
        if (req.price() != null) g.setPrice(req.price());
        if (req.quantity() != null) g.setQuantity(req.quantity());
        if (req.imageUrl() != null) g.setImageUrl(req.imageUrl());

        if (req.categoryId() != null) {
            Category category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            g.setCategory(category);
        }

        if (req.brandId() != null) {
            Brand brand = brandRepository.findById(req.brandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
            g.setBrand(brand);
        }

        Glasses saved = glassesRepository.save(g);

        return new GlassesAdminResponse(
                saved.getId(),
                saved.getName(),
                saved.getPrice(),
                saved.getQuantity(),
                saved.getCategory().getName(),
                saved.getBrand().getName(),
                saved.getImageUrl()
        );
    }


}
