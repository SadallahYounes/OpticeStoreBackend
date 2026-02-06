package com.opticstore.product.glasses.dto;

import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.product.glasses.model.GlassesImage;
import com.opticstore.utils.ImageUrlMapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
                    if (image == null || image.getImageUrl() == null) {
                        return null;
                    }

                    String imageUrl = image.getImageUrl().trim();

                    if (imageUrl.isEmpty()) {
                        return null;
                    }

                    // If already a full URL, return as is
                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        return imageUrl;
                    }

                    // Ensure proper formatting for relative URLs
                    if (baseUrl != null) {
                        // Remove leading slash from imageUrl if baseUrl already ends with slash
                        if (baseUrl.endsWith("/") && imageUrl.startsWith("/")) {
                            imageUrl = imageUrl.substring(1);
                        }
                        // Add slash if baseUrl doesn't end with it and imageUrl doesn't start with it
                        else if (!baseUrl.endsWith("/") && !imageUrl.startsWith("/")) {
                            imageUrl = "/" + imageUrl;
                        }
                        return baseUrl + imageUrl;
                    }

                    return imageUrl;
                })
                .filter(Objects::nonNull) // Filter out null URLs
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