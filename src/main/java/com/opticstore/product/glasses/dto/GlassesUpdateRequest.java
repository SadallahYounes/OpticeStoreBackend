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

        // CHANGE: Separate keep vs new images
        List<String> keepImageUrls,      // URLs of existing images to keep
        List<String> newImageUrls,       // URLs of new images to add
        Boolean active,

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
    // Helper method to get total images (keep + new)
    public int totalImageCount() {
        return (keepImageUrls != null ? keepImageUrls.size() : 0) +
                (newImageUrls != null ? newImageUrls.size() : 0);
    }
}