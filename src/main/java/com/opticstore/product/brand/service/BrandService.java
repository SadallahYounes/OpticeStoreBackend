package com.opticstore.product.brand.service;

import com.opticstore.product.brand.dto.BrandCreateRequest;
import com.opticstore.product.brand.dto.BrandResponse;
import com.opticstore.product.brand.dto.BrandUpdateRequest;
import com.opticstore.product.brand.model.Brand;
import com.opticstore.product.brand.repository.BrandRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {

    private final BrandRepository repository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public BrandService(BrandRepository repository) {
        this.repository = repository;
    }

    private String getFullLogoUrl(String logoPath) {
        if (logoPath == null || logoPath.isEmpty()) {
            return null;
        }
        if (logoPath.startsWith("http://") || logoPath.startsWith("https://")) {
            return logoPath;
        }
        return baseUrl + logoPath;
    }

    public List<BrandResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(b -> new BrandResponse(
                        b.getId(),
                        b.getName(),
                        getFullLogoUrl(b.getLogoUrl())
                ))
                .toList();
    }


    public BrandResponse getById(Long id) {
        Brand brand = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                getFullLogoUrl(brand.getLogoUrl())
        );
    }

    public BrandResponse create(BrandCreateRequest request) {
        // Validate brand name
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Brand name is required");
        }

        // Check if brand with same name already exists
        if (repository.existsByNameIgnoreCase(request.name().trim())) {
            throw new IllegalArgumentException("Brand with name '" + request.name() + "' already exists");
        }

        // Create new brand
        Brand brand = new Brand();
        brand.setName(request.name().trim());
        brand.setLogoUrl(request.logoUrl());

        Brand savedBrand = repository.save(brand);
        return toResponse(savedBrand);
    }

    // NEW: Update method
    public BrandResponse update(Long id, BrandUpdateRequest request) {
        // Find existing brand
        Brand brand = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with ID: " + id));

        // Validate new name if provided
        if (request.name() != null && !request.name().trim().isEmpty()) {
            String newName = request.name().trim();

            // Check if another brand already has this name (excluding current brand)
            if (!brand.getName().equalsIgnoreCase(newName) &&
                    repository.existsByNameIgnoreCase(newName)) {
                throw new IllegalArgumentException("Brand with name '" + newName + "' already exists");
            }

            brand.setName(newName);
        }

        // Update logo if provided
        if (request.logoUrl() != null) {
            brand.setLogoUrl(request.logoUrl());
        }

        Brand updatedBrand = repository.save(brand);
        return toResponse(updatedBrand);
    }

    // NEW: Delete method
    @Transactional
    public void delete(Long id) {
        Brand brand = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with ID: " + id));

        // Optional: Check if brand has associated products
        // You might want to prevent deletion if there are associated products
        // For now, we'll just delete

        repository.delete(brand);
    }

    // Helper method to convert Entity to Response
    private BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getLogoUrl()
        );
    }

}