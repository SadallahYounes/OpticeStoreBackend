package com.opticstore.product.glasses.service;

import com.opticstore.product.brand.model.Brand;
import com.opticstore.product.category.model.Category;
import com.opticstore.product.glasses.dto.GlassesCreateRequest;
import com.opticstore.product.glasses.dto.GlassesResponse;
import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.product.glasses.repository.GlassesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlassesService {

    private final GlassesRepository repository;

    public GlassesService(GlassesRepository repository) {
        this.repository = repository;
    }

    public List<GlassesResponse> getByCategory(String slug) {
        return repository.findByCategory_Slug(slug)
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
        return repository.findByBrand_NameIgnoreCase(brand)
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

    // admin


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

        Glasses saved = repository.save(glasses);

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

        Glasses glasses = repository.findById(glassesId)
                .orElseThrow(() -> new RuntimeException("Glasses not found"));

        glasses.setQuantity(newQuantity);
    }

}
