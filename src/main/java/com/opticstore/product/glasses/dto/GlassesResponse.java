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
        String category,    // For other categories (sunglasses, etc.)
        String brand,
        String description,
        Integer quantity,
        boolean available,

        // Specifications (Strings for frontend)
        String frameMaterial,
        String lensMaterial,
        String frameColor,
        String lensColor,
        String gender,      // This is now the main gender field
        String frameWidth,
        String bridgeWidth,
        String templeLength,
        String lensWidth,
        String lensHeight
) {
    public static GlassesResponse fromEntity(Glasses glasses, ImageUrlMapper mapper) {

        List<String> fullImageUrls = glasses.getImages().stream()
                .sorted(Comparator.comparing(
                        GlassesImage::getOrder,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ))
                .map(image -> mapper.toFullUrl(image.getImageUrl()))
                .toList();

        // CHANGE: Handle nullable category
        String categoryName = glasses.getCategory() != null ? glasses.getCategory().getName() : null;

        return new GlassesResponse(
                glasses.getId(),
                glasses.getName(),
                glasses.getPrice(),
                fullImageUrls,
                categoryName,
                glasses.getBrand().getName(),
                glasses.getDescription(),
                glasses.getQuantity(),
                glasses.isInStock(),

                glasses.getFrameMaterial(),
                glasses.getLensMaterial(),
                glasses.getFrameColor(),
                glasses.getLensColor(),
                glasses.getGender().name(), // Now guaranteed not null
                toString(glasses.getFrameWidth()),
                toString(glasses.getBridgeWidth()),
                toString(glasses.getTempleLength()),
                toString(glasses.getLensWidth()),
                toString(glasses.getLensHeight())
        );
    }

    private static String toString(BigDecimal value) {
        return value != null ? value.toPlainString() : null;
    }
}
