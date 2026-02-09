package com.opticstore.Analytics.model;

import lombok.Data;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "analytics_snapshots")
@Data
public class AnalyticsData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date")
    private LocalDateTime snapshotDate;

    @Enumerated(EnumType.STRING)
    private TimePeriod period;

    // Revenue metrics
    @Column(name = "total_revenue")
    private Double totalRevenue;

    @Column(name = "daily_revenue")
    private Double dailyRevenue;

    @Column(name = "revenue_growth")
    private Double revenueGrowth;

    @Column(name = "average_order_value")
    private Double averageOrderValue;

    // Order metrics
    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "pending_orders")
    private Integer pendingOrders;

    @Column(name = "delivered_orders")
    private Integer deliveredOrders;

    @Column(name = "cancelled_orders")
    private Integer cancelledOrders;

    @Column(name = "conversion_rate")
    private Double conversionRate;

    // Customer metrics
    @Column(name = "new_customers")
    private Integer newCustomers;

    @Column(name = "returning_customers")
    private Integer returningCustomers;

    @Column(name = "customer_acquisition_cost")
    private Double customerAcquisitionCost;

    @Column(name = "customer_lifetime_value")
    private Double customerLifetimeValue;

    // Product metrics
    @Column(name = "top_product_id")
    private Long topProductId;

    @Column(name = "top_product_name")
    private String topProductName;

    @Column(name = "top_product_revenue")
    private Double topProductRevenue;

    @Column(name = "low_stock_items")
    private Integer lowStockItems;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    //getters and setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDateTime snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public TimePeriod getPeriod() {
        return period;
    }

    public void setPeriod(TimePeriod period) {
        this.period = period;
    }

    public Double getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(Double dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    public Double getRevenueGrowth() {
        return revenueGrowth;
    }

    public void setRevenueGrowth(Double revenueGrowth) {
        this.revenueGrowth = revenueGrowth;
    }

    public Double getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(Double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Integer pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Integer getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(Integer deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public Integer getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Integer cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Integer getNewCustomers() {
        return newCustomers;
    }

    public void setNewCustomers(Integer newCustomers) {
        this.newCustomers = newCustomers;
    }

    public Integer getReturningCustomers() {
        return returningCustomers;
    }

    public void setReturningCustomers(Integer returningCustomers) {
        this.returningCustomers = returningCustomers;
    }

    public Double getCustomerAcquisitionCost() {
        return customerAcquisitionCost;
    }

    public void setCustomerAcquisitionCost(Double customerAcquisitionCost) {
        this.customerAcquisitionCost = customerAcquisitionCost;
    }

    public Double getCustomerLifetimeValue() {
        return customerLifetimeValue;
    }

    public void setCustomerLifetimeValue(Double customerLifetimeValue) {
        this.customerLifetimeValue = customerLifetimeValue;
    }

    public Long getTopProductId() {
        return topProductId;
    }

    public void setTopProductId(Long topProductId) {
        this.topProductId = topProductId;
    }

    public String getTopProductName() {
        return topProductName;
    }

    public void setTopProductName(String topProductName) {
        this.topProductName = topProductName;
    }

    public Double getTopProductRevenue() {
        return topProductRevenue;
    }

    public void setTopProductRevenue(Double topProductRevenue) {
        this.topProductRevenue = topProductRevenue;
    }

    public Integer getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(Integer lowStockItems) {
        this.lowStockItems = lowStockItems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}