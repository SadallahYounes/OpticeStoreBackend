package com.opticstore.product.glasses.repository;

import com.opticstore.product.glasses.model.GlassesImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GlassesImageRepository extends JpaRepository<GlassesImage, Long> {
    List<GlassesImage> findByGlassesId(Long glassesId);
    void deleteByGlassesId(Long glassesId);
}