package com.opticstore.product.glasses.dto;

import java.math.BigDecimal;

public record GlassesAdminResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer quantity,
        String category,
        String brand,
        String imageUrl
) {}
