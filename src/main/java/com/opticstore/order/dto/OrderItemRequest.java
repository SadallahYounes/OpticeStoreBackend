package com.opticstore.order.dto;

import java.math.BigDecimal;

public record OrderItemRequest(
        Long glassId,
        String glassName,
        BigDecimal price,
        int quantity
) {}