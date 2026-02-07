package com.opticstore.product.glasses.repository;


import com.opticstore.product.glasses.model.Glasses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface GlassesRepository extends JpaRepository<Glasses, Long> {

    List<Glasses> findByCategory_Slug(String slug);

    List<Glasses> findByBrand_NameIgnoreCase(String name);

    @Query("SELECT g FROM Glasses g WHERE g.brand.id = :brandId AND g.active = true")
    List<Glasses> findByBrandIdAndActiveTrue(@Param("brandId") Long brandId);

    @Query("SELECT g FROM Glasses g WHERE g.category.slug = :categorySlug AND LOWER(g.brand.name) = LOWER(:brandName) AND g.active = true")
    List<Glasses> findByCategorySlugAndBrandNameIgnoreCaseAndActiveTrue(
            @Param("categorySlug") String categorySlug,
            @Param("brandName") String brandName
    );

    List<Glasses> findByActiveTrue();


    @Query("SELECT g FROM Glasses g WHERE g.brand.id = :brandId AND g.gender = :gender AND g.active = true")
    List<Glasses> findByBrandIdAndGenderAndActiveTrue(
            @Param("brandId") Long brandId,
            @Param("gender") String gender
    );
}