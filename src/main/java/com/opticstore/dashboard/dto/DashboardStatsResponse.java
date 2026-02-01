package com.opticstore.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DashboardStatsResponse(
        @JsonProperty("totalOrders") Long totalOrders,
        @JsonProperty("totalRevenue") Double totalRevenue,
        @JsonProperty("pendingOrders") Long pendingOrders,      // NEW orders
        @JsonProperty("deliveredOrders") Long deliveredOrders,
        @JsonProperty("todayOrders") Long todayOrders,
        @JsonProperty("activeCustomers") Long activeCustomers,
        @JsonProperty("averageOrderValue") Double averageOrderValue,

        @JsonProperty("orderGrowth") Double orderGrowth,
        @JsonProperty("revenueGrowth") Double revenueGrowth,
        @JsonProperty("pendingChange") Double pendingChange,
        @JsonProperty("deliveredChange") Double deliveredChange,

        @JsonProperty("dailyRevenue") Double dailyRevenue,
        @JsonProperty("weeklyRevenue") Double weeklyRevenue,
        @JsonProperty("monthlyRevenue") Double monthlyRevenue,

        // Optional: Additional status counts
        @JsonProperty("confirmedOrders") Long confirmedOrders,
        @JsonProperty("shippedOrders") Long shippedOrders,
        @JsonProperty("canceledOrders") Long canceledOrders
) {
    public DashboardStatsResponse {
        // Default values if null
        if (totalOrders == null) totalOrders = 0L;
        if (totalRevenue == null) totalRevenue = 0.0;
        if (pendingOrders == null) pendingOrders = 0L;
        if (deliveredOrders == null) deliveredOrders = 0L;
        if (todayOrders == null) todayOrders = 0L;
        if (activeCustomers == null) activeCustomers = 0L;
        if (averageOrderValue == null) averageOrderValue = 0.0;

        if (orderGrowth == null) orderGrowth = 0.0;
        if (revenueGrowth == null) revenueGrowth = 0.0;
        if (pendingChange == null) pendingChange = 0.0;
        if (deliveredChange == null) deliveredChange = 0.0;

        if (dailyRevenue == null) dailyRevenue = 0.0;
        if (weeklyRevenue == null) weeklyRevenue = 0.0;
        if (monthlyRevenue == null) monthlyRevenue = 0.0;

        if (confirmedOrders == null) confirmedOrders = 0L;
        if (shippedOrders == null) shippedOrders = 0L;
        if (canceledOrders == null) canceledOrders = 0L;
    }

    // Constructor without additional statuses (for backward compatibility)
    public DashboardStatsResponse(
            Long totalOrders,
            Double totalRevenue,
            Long pendingOrders,
            Long deliveredOrders,
            Long todayOrders,
            Long activeCustomers,
            Double averageOrderValue,
            Double orderGrowth,
            Double revenueGrowth,
            Double pendingChange,
            Double deliveredChange,
            Double dailyRevenue,
            Double weeklyRevenue,
            Double monthlyRevenue
    ) {
        this(
                totalOrders, totalRevenue, pendingOrders, deliveredOrders,
                todayOrders, activeCustomers, averageOrderValue,
                orderGrowth, revenueGrowth, pendingChange, deliveredChange,
                dailyRevenue, weeklyRevenue, monthlyRevenue,
                0L, 0L, 0L  // Default values for additional statuses
        );
    }
}