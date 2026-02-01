package com.opticstore.product.glasses.dto;

import java.math.BigDecimal;

public record GlassesResponse(
        Long id,
        String name,
        BigDecimal price,
        String imageUrl,
        String category,
        String brand
) {}