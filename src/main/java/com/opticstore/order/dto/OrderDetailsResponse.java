package com.opticstore.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String wilaya,
        String baladia,
        String address,
        BigDecimal total,
        String status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {}