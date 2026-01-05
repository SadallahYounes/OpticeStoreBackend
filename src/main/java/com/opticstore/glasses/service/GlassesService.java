package com.opticstore.glasses.service;

import com.opticstore.glasses.dto.GlassesResponse;
import com.opticstore.glasses.repository.GlassesRepository;
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
}
