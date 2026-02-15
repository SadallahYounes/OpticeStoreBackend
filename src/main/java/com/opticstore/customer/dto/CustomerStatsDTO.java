package com.opticstore.customer.dto;

import java.math.BigDecimal;
import java.util.Map;

public class CustomerStatsDTO {
    private Long totalCustomers;
    private Long activeCustomers;
    private Long newCustomersThisMonth;
    private Long repeatCustomers;
    private Double repeatRate;
    private BigDecimal averageLifetimeValue;
    private BigDecimal totalRevenue;
    private Map<String, Long> customersByWilaya;
    private Map<String, Long> customersBySegment;
    private Double activePercentage;
    private Long oneTimeCustomers;
    private Long vipCustomers;
    private Long regularCustomers;
    private Long inactiveCustomers;

    // Getters and setters

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public Long getActiveCustomers() {
        return activeCustomers;
    }

    public void setActiveCustomers(Long activeCustomers) {
        this.activeCustomers = activeCustomers;
    }

    public Long getNewCustomersThisMonth() {
        return newCustomersThisMonth;
    }

    public void setNewCustomersThisMonth(Long newCustomersThisMonth) {
        this.newCustomersThisMonth = newCustomersThisMonth;
    }

    public Long getRepeatCustomers() {
        return repeatCustomers;
    }

    public void setRepeatCustomers(Long repeatCustomers) {
        this.repeatCustomers = repeatCustomers;
    }

    public Double getRepeatRate() {
        return repeatRate;
    }

    public void setRepeatRate(Double repeatRate) {
        this.repeatRate = repeatRate;
    }

    public BigDecimal getAverageLifetimeValue() {
        return averageLifetimeValue;
    }

    public void setAverageLifetimeValue(BigDecimal averageLifetimeValue) {
        this.averageLifetimeValue = averageLifetimeValue;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Map<String, Long> getCustomersByWilaya() {
        return customersByWilaya;
    }

    public void setCustomersByWilaya(Map<String, Long> customersByWilaya) {
        this.customersByWilaya = customersByWilaya;
    }

    public Map<String, Long> getCustomersBySegment() {
        return customersBySegment;
    }

    public void setCustomersBySegment(Map<String, Long> customersBySegment) {
        this.customersBySegment = customersBySegment;
    }

    public Double getActivePercentage() {
        return activePercentage;
    }

    public void setActivePercentage(Double activePercentage) {
        this.activePercentage = activePercentage;
    }

    public Long getOneTimeCustomers() {
        return oneTimeCustomers;
    }

    public void setOneTimeCustomers(Long oneTimeCustomers) {
        this.oneTimeCustomers = oneTimeCustomers;
    }

    public Long getVipCustomers() {
        return vipCustomers;
    }

    public void setVipCustomers(Long vipCustomers) {
        this.vipCustomers = vipCustomers;
    }

    public Long getRegularCustomers() {
        return regularCustomers;
    }

    public void setRegularCustomers(Long regularCustomers) {
        this.regularCustomers = regularCustomers;
    }

    public Long getInactiveCustomers() {
        return inactiveCustomers;
    }

    public void setInactiveCustomers(Long inactiveCustomers) {
        this.inactiveCustomers = inactiveCustomers;
    }
}