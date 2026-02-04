package com.opticstore.product.glasses.dto;

import java.math.BigDecimal;
import java.util.List;

public record GlassesUpdateRequest(
        String name,
        BigDecimal price,
        String description,
        Integer quantity,
        Long categoryId,
        Long brandId,
        List<String> imageUrls,
        Boolean active,

        String frameMaterial,
        String lensMaterial,
        String frameColor,
        String lensColor,
        String gender,

        // Measurements as String
        String frameWidth,
        String bridgeWidth,
        String templeLength,
        String lensWidth,
        String lensHeight
) {}
