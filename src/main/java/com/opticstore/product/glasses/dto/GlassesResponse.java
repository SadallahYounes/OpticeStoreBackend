package com.opticstore.product.glasses.dto;

import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.utils.ImageUrlMapper;

import java.math.BigDecimal;

public record GlassesResponse(
        Long id,
        String name,
        BigDecimal price,
        String imageUrl,
        String category,
        String brand
) {
    // Factory method that uses ImageUrlMapper
    public static GlassesResponse fromEntity(Glasses glasses, ImageUrlMapper mapper) {
        return new GlassesResponse(
                glasses.getId(),
                glasses.getName(),
                glasses.getPrice(),
                mapper.toFullUrl(glasses.getImageUrl()),
                glasses.getCategory().getName(),
                glasses.getBrand().getName()
        );
    }
}