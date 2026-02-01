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
import com.opticstore.utils.ImageUrlMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class GlassesService {

    private final GlassesRepository glassesRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public GlassesService(GlassesRepository glassesRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository) {
        this.glassesRepository = glassesRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    // Helper method for full image URLs
    private String getFullImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }
        return baseUrl + imagePath;
    }

    // ============== CUSTOMER METHODS ==============

    public List<GlassesResponse> getByCategory(String slug) {
        return glassesRepository.findByCategory_Slug(slug)
                .stream()
                .filter(Glasses::isActive)
                .map(g -> new GlassesResponse(
                        g.getId(),
                        g.getName(),
                        g.getPrice(),
                        getFullImageUrl(g.getImageUrl()),
                        g.getCategory().getName(),
                        g.getBrand().getName()
                ))
                .toList();
    }

    public List<GlassesResponse> getByBrand(String brand) {
        return glassesRepository.findByBrand_NameIgnoreCase(brand)
                .stream()
                .filter(Glasses::isActive)
                .map(g -> new GlassesResponse(
                        g.getId(),
                        g.getName(),
                        g.getPrice(),
                        getFullImageUrl(g.getImageUrl()),
                        g.getCategory().getName(),
                        g.getBrand().getName()
                ))
                .toList();
    }

    public List<GlassesResponse> getAllForCustomers() {
        return glassesRepository.findByActiveTrue()
                .stream()
                .map(g -> new GlassesResponse(
                        g.getId(),
                        g.getName(),
                        g.getPrice(),
                        getFullImageUrl(g.getImageUrl()),
                        g.getCategory().getName(),
                        g.getBrand().getName()
                ))
                .toList();
    }

    // ============== ADMIN METHODS ==============

    public GlassesResponse create(GlassesCreateRequest request, Category category, Brand brand) {
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
                getFullImageUrl(saved.getImageUrl()),
                saved.getCategory().getName(),
                saved.getBrand().getName()
        );
    }

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
                        getFullImageUrl(g.getImageUrl()),
                        g.isActive()
                ))
                .toList();
    }

    public GlassesAdminResponse updateGlass(Long id, GlassesUpdateRequest req) {
        Glasses g = glassesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Glass not found"));

        if (req.name() != null) g.setName(req.name());
        if (req.price() != null) g.setPrice(req.price());
        if (req.quantity() != null) {
            if (req.quantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            g.setQuantity(req.quantity());
        }
        if (req.imageUrl() != null) g.setImageUrl(req.imageUrl());
        if (req.active() != null) g.setActive(req.active());

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
                getFullImageUrl(saved.getImageUrl()),
                saved.isActive()
        );
    }

    // KEEP THIS DELETE METHOD!
    public void delete(Long id) {
        Glasses g = glassesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Glass not found"));

        // Check if already deleted
        if (!g.isActive()) {
            throw new RuntimeException("Product is already deleted");
        }

        g.setActive(false);
        glassesRepository.save(g);
    }
}