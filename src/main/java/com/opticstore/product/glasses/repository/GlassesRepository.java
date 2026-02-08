package com.opticstore.product.glasses.repository;


import com.opticstore.product.glasses.model.Glasses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface GlassesRepository extends JpaRepository<Glasses, Long> {

    // deprecated
    @Deprecated
    List<Glasses> findByCategory_Slug(String slug);

    // Gender-based queries (keep these)
    @Query("SELECT g FROM Glasses g WHERE g.gender = :gender AND g.active = true")
    List<Glasses> findByGenderAndActiveTrue(@Param("gender") Glasses.Gender gender);

    List<Glasses> findByBrand_NameIgnoreCase(String name);

    @Query("SELECT g FROM Glasses g WHERE g.brand.id = :brandId AND g.active = true")
    List<Glasses> findByBrandIdAndActiveTrue(@Param("brandId") Long brandId);

    @Query("SELECT g FROM Glasses g WHERE g.gender = :gender AND LOWER(g.brand.name) = LOWER(:brandName) AND g.active = true")
    List<Glasses> findByGenderAndBrandNameIgnoreCaseAndActiveTrue(
            @Param("gender") Glasses.Gender gender,
            @Param("brandName") String brandName
    );

    List<Glasses> findByActiveTrue();

    @Query("SELECT g FROM Glasses g WHERE g.brand.id = :brandId AND g.gender = :gender AND g.active = true")
    List<Glasses> findByBrandIdAndGenderAndActiveTrue(
            @Param("brandId") Long brandId,
            @Param("gender") Glasses.Gender gender
    );

    // Keep this for admin purposes if needed
    @Query("SELECT g FROM Glasses g WHERE g.category.slug = :categorySlug AND g.active = true")
    List<Glasses> findByCategorySlugAndActiveTrue(@Param("categorySlug") String categorySlug);
}