package com.opticstore.product.glasses.dto;

import java.math.BigDecimal;
import java.util.List;

public record GlassesUpdateRequest(
        String name,
        BigDecimal price,
        Integer quantity,
        Long categoryId,
        Long brandId,
        List<String> imageUrls,
        Boolean active
) {}