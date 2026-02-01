package com.opticstore.product.brand.service;

import com.opticstore.product.brand.dto.BrandResponse;
import com.opticstore.product.brand.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {

    private final BrandRepository repository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;  // Use same approach as glasses

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
                        getFullLogoUrl(b.getLogoUrl())  // Use helper method
                ))
                .toList();
    }
}