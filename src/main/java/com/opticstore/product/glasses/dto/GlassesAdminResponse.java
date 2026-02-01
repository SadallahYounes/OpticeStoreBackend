package com.opticstore.product.glasses.dto;

import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.utils.ImageUrlMapper;

import java.math.BigDecimal;

public record GlassesAdminResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer quantity,
        String category,
        String brand,
        String imageUrl,
        boolean active
) {
    // Factory method that uses ImageUrlMapper
    public static GlassesAdminResponse fromEntity(Glasses glasses, String baseUrl) {
        String fullImageUrl = glasses.getImageUrl() != null
                ? baseUrl + glasses.getImageUrl()
                : null;

        return new GlassesAdminResponse(
                glasses.getId(),
                glasses.getName(),
                glasses.getPrice(),
                glasses.getQuantity(),
                glasses.getCategory().getName(),
                glasses.getBrand().getName(),
                fullImageUrl,
                glasses.isActive()
        );
    }
}