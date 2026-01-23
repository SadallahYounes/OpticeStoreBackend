package com.opticstore.order.history.dto;

import java.time.LocalDateTime;

public record OrderStatusHistoryResponse(
        String oldStatus,
        String newStatus,
        String changedBy,
        LocalDateTime changedAt
) {}