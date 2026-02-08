package com.opticstore.product.brand.repository;

import com.opticstore.product.brand.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    // Check if brand with name exists (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // Find by name (case-insensitive)
    Optional<Brand> findByNameIgnoreCase(String name);
}
