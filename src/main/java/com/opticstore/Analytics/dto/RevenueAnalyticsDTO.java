package com.opticstore.Analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsDTO {
    private BigDecimal totalRevenue;
    private BigDecimal revenueGrowth;
    private BigDecimal averageOrderValue;
    private List<TimeSeriesData> dailyRevenue;
    private Map<String, BigDecimal> revenueByCategory;
    private Map<String, BigDecimal> revenueByBrand;

    // getters and setters
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getRevenueGrowth() { return revenueGrowth; }
    public void setRevenueGrowth(BigDecimal revenueGrowth) { this.revenueGrowth = revenueGrowth; }

    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public List<TimeSeriesData> getDailyRevenue() { return dailyRevenue; }
    public void setDailyRevenue(List<TimeSeriesData> dailyRevenue) { this.dailyRevenue = dailyRevenue; }

    public Map<String, BigDecimal> getRevenueByCategory() { return revenueByCategory; }
    public void setRevenueByCategory(Map<String, BigDecimal> revenueByCategory) { this.revenueByCategory = revenueByCategory; }

    public Map<String, BigDecimal> getRevenueByBrand() { return revenueByBrand; }
    public void setRevenueByBrand(Map<String, BigDecimal> revenueByBrand) { this.revenueByBrand = revenueByBrand; }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        private LocalDate date;
        private BigDecimal value;
        private Integer orders;

        public TimeSeriesData(){}
        // Manual getters and setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }

        public Integer getOrders() { return orders; }
        public void setOrders(Integer orders) { this.orders = orders; }

        public TimeSeriesData(LocalDate date, BigDecimal value) {
            this.date = date;
            this.value = value;
            this.orders = 0;
        }

        public TimeSeriesData(LocalDate date, BigDecimal value, int orders) {
            this.date = date;
            this.value = value;
            this.orders = orders;
        }
    }
}