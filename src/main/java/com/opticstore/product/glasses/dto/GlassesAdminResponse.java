package com.opticstore.product.glasses.dto;

import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.product.glasses.model.GlassesImage;
import com.opticstore.utils.ImageUrlMapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record GlassesAdminResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer quantity,
        String category,
        String brand,
        List<String> imageUrls,
        boolean active
) {
    // Factory method that uses ImageUrlMapper
    public static GlassesAdminResponse fromEntity(Glasses glasses, String baseUrl) {
        List<String> fullImageUrls = glasses.getImages().stream()
                .sorted(Comparator.comparing(GlassesImage::getOrder,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(image -> {
                    String imageUrl = image.getImageUrl();
                    if (imageUrl != null && !imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                        return baseUrl + imageUrl;
                    }
                    return imageUrl;
                })
                .collect(Collectors.toList());

        return new GlassesAdminResponse(
                glasses.getId(),
                glasses.getName(),
                glasses.getPrice(),
                glasses.getQuantity(),
                glasses.getCategory().getName(),
                glasses.getBrand().getName(),
                fullImageUrls,
                glasses.isActive()
        );
    }
}