package com.opticstore.Analytics.service;

import com.opticstore.Analytics.dto.AnalyticsRequestDTO;
import com.opticstore.Analytics.dto.CustomerAnalyticsDTO;
import com.opticstore.Analytics.dto.ProductPerformanceDTO;
import com.opticstore.Analytics.dto.RevenueAnalyticsDTO;
import com.opticstore.Analytics.model.AnalyticsFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    // Revenue Analytics
    RevenueAnalyticsDTO getRevenueAnalytics(AnalyticsRequestDTO request);
    Map<String, Object> getRevenueTrends(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getRevenueByCategory(AnalyticsFilter filter);
    List<Map<String, Object>> getRevenueByBrand(AnalyticsFilter filter);

    // Product Analytics
    ProductPerformanceDTO getProductPerformance(AnalyticsFilter filter);
    List<Map<String, Object>> getTopProducts(AnalyticsFilter filter);
    List<Map<String, Object>> getLowStockProducts(int threshold);
    Map<String, Object> getInventoryTurnover();

    // Customer Analytics
    CustomerAnalyticsDTO getCustomerAnalytics(AnalyticsFilter filter);
    List<Map<String, Object>> getCustomerSegments();
    Map<String, Object> getCustomerRetention(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getCustomerAcquisitionSources();

    // Prescription Analytics (specific to optical store)
    Map<String, Object> getPrescriptionAnalytics(AnalyticsFilter filter);
    List<Map<String, Object>> getPrescriptionTrends();
    Map<String, Object> getLensTypePopularity();

    // Export functionality
    byte[] exportAnalyticsReport(AnalyticsRequestDTO request, String format);

    // Data refresh
    void refreshAnalyticsData();
}