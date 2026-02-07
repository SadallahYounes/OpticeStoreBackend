package com.opticstore.product.brand.service;

import com.opticstore.product.brand.dto.BrandResponse;
import com.opticstore.product.brand.model.Brand;
import com.opticstore.product.brand.repository.BrandRepository;
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

    // ADD THIS METHOD
    public BrandResponse getById(Long id) {
        Brand brand = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                getFullLogoUrl(brand.getLogoUrl())
        );
    }
}