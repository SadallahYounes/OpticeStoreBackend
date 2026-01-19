package com.opticstore.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long glassId,
        String glassName,
        BigDecimal price,
        int quantity
) {}