package com.opticstore.customer.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CustomerDetailsDTO extends CustomerDTO {
    private List<OrderSummaryDTO> recentOrders;
    private Map<String, Object> insights;
    private List<String> notes;
    private Map<String, Integer> ordersByStatus;
    private List<Map<String, Object>> topProducts;
    private String favoriteCategory;
    private String favoriteBrand;
    private BigDecimal averageOrderValue;
    private Integer daysSinceLastOrder;
    private String customerSegment;

    public List<OrderSummaryDTO> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderSummaryDTO> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public Map<String, Object> getInsights() {
        return insights;
    }

    public void setInsights(Map<String, Object> insights) {
        this.insights = insights;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public Map<String, Integer> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Integer> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public List<Map<String, Object>> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<Map<String, Object>> topProducts) {
        this.topProducts = topProducts;
    }

    public String getFavoriteCategory() {
        return favoriteCategory;
    }

    public void setFavoriteCategory(String favoriteCategory) {
        this.favoriteCategory = favoriteCategory;
    }

    public String getFavoriteBrand() {
        return favoriteBrand;
    }

    public void setFavoriteBrand(String favoriteBrand) {
        this.favoriteBrand = favoriteBrand;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public Integer getDaysSinceLastOrder() {
        return daysSinceLastOrder;
    }

    public void setDaysSinceLastOrder(Integer daysSinceLastOrder) {
        this.daysSinceLastOrder = daysSinceLastOrder;
    }

    public String getCustomerSegment() {
        return customerSegment;
    }

    public void setCustomerSegment(String customerSegment) {
        this.customerSegment = customerSegment;
    }
}