package com.opticstore.product.glasses.dto;

import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.product.glasses.model.GlassesImage;
import com.opticstore.utils.ImageUrlMapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record GlassesResponse(
        Long id,
        String name,
        BigDecimal price,
        List<String> imageUrls,
        String category,
        String brand
) {
    // Factory method that uses ImageUrlMapper
    public static GlassesResponse fromEntity(Glasses glasses, ImageUrlMapper mapper) {
        List<String> fullImageUrls = glasses.getImages().stream()
                .sorted(Comparator.comparing(GlassesImage::getOrder,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(image -> mapper.toFullUrl(image.getImageUrl()))
                .collect(Collectors.toList());

        return new GlassesResponse(
                glasses.getId(),
                glasses.getName(),
                glasses.getPrice(),
                fullImageUrls,
                glasses.getCategory().getName(),
                glasses.getBrand().getName()
        );
    }
}