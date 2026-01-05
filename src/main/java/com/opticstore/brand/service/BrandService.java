package com.opticstore.brand.service;

import com.opticstore.brand.dto.BrandResponse;
import com.opticstore.brand.repository.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {

    private final BrandRepository repository;

    public BrandService(BrandRepository repository) {
        this.repository = repository;
    }

    public List<BrandResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(b -> new BrandResponse(
                        b.getId(),
                        b.getName(),
                        b.getLogoUrl()
                ))
                .toList();
    }
}
