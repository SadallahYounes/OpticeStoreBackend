package com.opticstore.order.dto;

import com.opticstore.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderResponse(
        Long id,
        String customerName,
        String phone,
        String wilaya,
        String baladia,
        BigDecimal total,
        String status,
        LocalDateTime createdAt
) {}