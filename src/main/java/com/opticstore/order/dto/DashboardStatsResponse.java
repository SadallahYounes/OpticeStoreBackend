package com.opticstore.order.dto;

import java.math.BigDecimal;

public record DashboardStatsResponse(
        long totalOrders,
        BigDecimal totalRevenue,
        long pendingOrders,
        long deliveredOrders
) {}