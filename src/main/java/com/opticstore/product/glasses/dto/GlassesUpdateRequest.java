package com.opticstore.product.glasses.dto;

import java.math.BigDecimal;

public record GlassesUpdateRequest(
        String name,
        BigDecimal price,
        Integer quantity,
        Long categoryId,
        Long brandId,
        String imageUrl
) {}
