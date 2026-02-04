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
        String brand,
        String description,
        Integer quantity,
        boolean available,

        // Specifications (Strings for frontend)
        String frameMaterial,
        String lensMaterial,
        String frameColor,
        String lensColor,
        String gender,
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

        return new GlassesResponse(
                glasses.getId(),
                glasses.getName(),
                glasses.getPrice(),
                fullImageUrls,
                glasses.getCategory().getName(),
                glasses.getBrand().getName(),
                glasses.getDescription(),
                glasses.getQuantity(),
                glasses.isInStock(),

                glasses.getFrameMaterial(),
                glasses.getLensMaterial(),
                glasses.getFrameColor(),
                glasses.getLensColor(),
                glasses.getGender() != null ? glasses.getGender().name() : null,
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
