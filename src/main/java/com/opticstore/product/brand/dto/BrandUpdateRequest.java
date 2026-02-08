package com.opticstore.product.brand.dto;

import jakarta.validation.constraints.NotBlank;

public record BrandUpdateRequest(
        @NotBlank(message = "Brand name is required")
        String name,
        String logoUrl
) {}