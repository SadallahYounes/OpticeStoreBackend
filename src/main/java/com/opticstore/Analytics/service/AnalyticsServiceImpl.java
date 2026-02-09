package com.opticstore.Analytics.service;


import com.opticstore.Analytics.dto.AnalyticsRequestDTO;
import com.opticstore.Analytics.dto.CustomerAnalyticsDTO;
import com.opticstore.Analytics.dto.ProductPerformanceDTO;
import com.opticstore.Analytics.dto.RevenueAnalyticsDTO;
import com.opticstore.Analytics.model.AnalyticsFilter;
import com.opticstore.Analytics.model.TimePeriod;
import com.opticstore.order.model.Order;
import com.opticstore.order.repository.OrderItemRepository;
import com.opticstore.order.repository.OrderRepository;
import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.product.glasses.repository.GlassesRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final GlassesRepository glassesRepository;

    public AnalyticsServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, GlassesRepository glassesRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.glassesRepository = glassesRepository;
    }

    @Override
    public RevenueAnalyticsDTO getRevenueAnalytics(AnalyticsRequestDTO request) {
        RevenueAnalyticsDTO response = new RevenueAnalyticsDTO();

        DateRange dateRange = getDateRange(request);
        LocalDateTime startDateTime = dateRange.getStartDateTime();
        LocalDateTime endDateTime = dateRange.getEndDateTime();

        // Get total revenue
        BigDecimal totalRevenue = orderRepository.sumRevenueBetweenDates(startDateTime, endDateTime);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        response.setTotalRevenue(totalRevenue);

        // Get order count
        Long orderCount = orderRepository.countByCreatedAtBetween(startDateTime, endDateTime);

        // Calculate average order value
        if (orderCount != null && orderCount > 0 && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal avgOrderValue = totalRevenue.divide(
                    BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP
            );
            response.setAverageOrderValue(avgOrderValue);
        } else {
            response.setAverageOrderValue(BigDecimal.ZERO);
        }

        // Get daily revenue trends
        List<Object[]> dailyRevenueData = getDailyRevenueData(startDateTime, endDateTime);
        List<RevenueAnalyticsDTO.TimeSeriesData> dailyRevenue = new ArrayList<>();

        for (Object[] data : dailyRevenueData) {
            try {
                RevenueAnalyticsDTO.TimeSeriesData tsData = new RevenueAnalyticsDTO.TimeSeriesData();

                // Handle date
                if (data[0] instanceof java.sql.Date) {
                    tsData.setDate(((java.sql.Date) data[0]).toLocalDate());
                } else if (data[0] instanceof LocalDate) {
                    tsData.setDate((LocalDate) data[0]);
                } else if (data[0] instanceof java.util.Date) {
                    tsData.setDate(new java.sql.Date(((java.util.Date) data[0]).getTime()).toLocalDate());
                }

                // Handle revenue value
                if (data[1] != null) {
                    tsData.setValue(new BigDecimal(data[1].toString()));
                } else {
                    tsData.setValue(BigDecimal.ZERO);
                }

                // Handle order count
                if (data[2] != null) {
                    tsData.setOrders(((Number) data[2]).intValue());
                } else {
                    tsData.setOrders(0);
                }

                dailyRevenue.add(tsData);
            } catch (Exception e) {
                System.err.println("Error processing daily revenue data: " + e.getMessage());
            }
        }

        response.setDailyRevenue(dailyRevenue);

        // Get revenue by category
        try {
            Map<String, BigDecimal> revenueByCategory = getRevenueByCategory(startDateTime, endDateTime);
            response.setRevenueByCategory(revenueByCategory);
        } catch (Exception e) {
            System.err.println("Could not get revenue by category: " + e.getMessage());
            response.setRevenueByCategory(new HashMap<>());
        }

        // Get revenue by brand
        try {
            Map<String, BigDecimal> revenueByBrand = getRevenueByBrand(startDateTime, endDateTime);
            response.setRevenueByBrand(revenueByBrand);
        } catch (Exception e) {
            System.err.println("Could not get revenue by brand: " + e.getMessage());
            response.setRevenueByBrand(new HashMap<>());
        }

        return response;
    }

    private List<Object[]> getDailyRevenueData(LocalDateTime start, LocalDateTime end) {
        try {
            // Try to use the new query
            return orderRepository.getDailyRevenueBetweenDates(start, end);
        } catch (Exception e) {
            System.err.println("Error in getDailyRevenueData: " + e.getMessage());
            // Fallback to existing salesByDay method
            List<Object[]> allSales = orderRepository.salesByDay();
            return allSales.stream()
                    .filter(data -> {
                        try {
                            if (data[0] instanceof java.sql.Date) {
                                LocalDate saleDate = ((java.sql.Date) data[0]).toLocalDate();
                                return !saleDate.isBefore(start.toLocalDate()) &&
                                        !saleDate.isAfter(end.toLocalDate());
                            }
                            return false;
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    private Map<String, BigDecimal> getRevenueByCategory(LocalDateTime start, LocalDateTime end) {
        try {
            List<Object[]> categoryData = orderItemRepository.getRevenueByCategory(start, end);
            Map<String, BigDecimal> result = new HashMap<>();

            for (Object[] data : categoryData) {
                if (data[0] != null && data[1] != null) {
                    String category = data[0].toString();
                    BigDecimal revenue = new BigDecimal(data[1].toString());
                    result.put(category, revenue);
                }
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error getting revenue by category: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<String, BigDecimal> getRevenueByBrand(LocalDateTime start, LocalDateTime end) {
        try {
            List<Object[]> brandData = orderItemRepository.getRevenueByBrand(start, end);
            Map<String, BigDecimal> result = new HashMap<>();

            for (Object[] data : brandData) {
                if (data[0] != null && data[1] != null) {
                    String brand = data[0].toString();
                    BigDecimal revenue = new BigDecimal(data[1].toString());
                    result.put(brand, revenue);
                }
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error getting revenue by brand: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private List<Object[]> getTopGlassesByPeriod(LocalDateTime start, LocalDateTime end, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return orderItemRepository.getTopSellingGlasses(start, end, pageable);
        } catch (Exception e) {
            System.err.println("Error getting top glasses: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ProductPerformanceDTO getProductPerformance(AnalyticsFilter filter) {
        ProductPerformanceDTO response = new ProductPerformanceDTO();

        DateRange dateRange = getDateRangeFromFilter(filter);
        LocalDateTime startDateTime = dateRange.getStartDateTime();
        LocalDateTime endDateTime = dateRange.getEndDateTime();

        // Get top selling glasses
        List<ProductPerformanceDTO.ProductStats> topProducts = new ArrayList<>();

        try {
            List<Object[]> topGlassesData = getTopGlassesByPeriod(startDateTime, endDateTime, filter.getLimit());

            for (Object[] data : topGlassesData) {
                try {
                    ProductPerformanceDTO.ProductStats stats = new ProductPerformanceDTO.ProductStats();

                    if (data[0] != null) {
                        stats.setProductId(((Number) data[0]).longValue());
                    }

                    if (data[1] != null) {
                        stats.setProductName(data[1].toString());
                    }

                    if (data[2] != null) {
                        stats.setBrand(data[2].toString());
                    }

                    if (data[3] != null) {
                        stats.setCategory(data[3].toString());
                    }

                    if (data[4] != null) {
                        stats.setUnitsSold(((Number) data[4]).intValue());
                    }

                    if (data[5] != null) {
                        stats.setRevenue(new BigDecimal(data[5].toString()));
                    }

                    // Get stock quantity for this glasses
                    if (data[0] != null) {
                        Long glassId = ((Number) data[0]).longValue();
                        Optional<Glasses> glassOpt = glassesRepository.findById(glassId);
                        if (glassOpt.isPresent()) {
                            Glasses glass = glassOpt.get();
                            stats.setStockQuantity(glass.getQuantity());

                            // Calculate stock value (quantity * price)
                            if (glass.getPrice() != null && glass.getQuantity() != null) {
                                BigDecimal stockValue = glass.getPrice()
                                        .multiply(BigDecimal.valueOf(glass.getQuantity()));
                                stats.setStockValue(stockValue);
                            }
                        }
                    }

                    topProducts.add(stats);
                } catch (Exception e) {
                    System.err.println("Error processing product stats: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting top products: " + e.getMessage());
        }

        response.setTopProducts(topProducts);

        // Set top product if available
        if (!topProducts.isEmpty()) {
            ProductPerformanceDTO.ProductStats topProductStats = topProducts.get(0);
            ProductPerformanceDTO.ProductSummary topProduct = new ProductPerformanceDTO.ProductSummary();
            topProduct.setId(topProductStats.getProductId());
            topProduct.setName(topProductStats.getProductName());
            topProduct.setBrand(topProductStats.getBrand());
            topProduct.setCategory(topProductStats.getCategory());
            topProduct.setTotalRevenue(topProductStats.getRevenue());
            topProduct.setUnitsSold(topProductStats.getUnitsSold());

            // Try to get image URL if available
            if (topProductStats.getProductId() != null) {
                Optional<Glasses> glassOpt = glassesRepository.findById(topProductStats.getProductId());
                if (glassOpt.isPresent()) {
                    Glasses glass = glassOpt.get();
                    // Get first image if available
                    if (glass.getImages() != null && !glass.getImages().isEmpty()) {
                        topProduct.setImageUrl("/api/images/" + glass.getImages().get(0).getId());
                    }
                }
            }

            response.setTopProduct(topProduct);
        }

        // Get inventory metrics
        ProductPerformanceDTO.InventoryMetrics inventoryMetrics = new ProductPerformanceDTO.InventoryMetrics();

        try {
            List<Glasses> allGlasses = glassesRepository.findByActiveTrue();
            int totalProducts = allGlasses.size();
            inventoryMetrics.setTotalProducts(totalProducts);

            // Count low stock items (quantity <= 5)
            int lowStockItems = (int) allGlasses.stream()
                    .filter(g -> g.getQuantity() != null && g.getQuantity() <= 5)
                    .count();
            inventoryMetrics.setLowStockItems(lowStockItems);

            // Count out of stock items
            int outOfStockItems = (int) allGlasses.stream()
                    .filter(g -> g.getQuantity() != null && g.getQuantity() <= 0)
                    .count();
            inventoryMetrics.setOutOfStockItems(outOfStockItems);

            // Calculate total stock value
            BigDecimal totalStockValue = allGlasses.stream()
                    .filter(g -> g.getPrice() != null && g.getQuantity() != null)
                    .map(g -> g.getPrice().multiply(BigDecimal.valueOf(g.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            inventoryMetrics.setTotalStockValue(totalStockValue);

            response.setInventoryMetrics(inventoryMetrics);
        } catch (Exception e) {
            System.err.println("Error calculating inventory metrics: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Map<String, Object> getRevenueTrends(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // Get daily trends
        List<Object[]> dailyData = getDailyRevenueData(startDateTime, endDateTime);

        Map<String, Object> trends = new HashMap<>();
        trends.put("daily", dailyData);
        trends.put("startDate", startDate);
        trends.put("endDate", endDate);

        // Calculate growth if we have previous period data
        if (dailyData != null && dailyData.size() > 1) {
            try {
                BigDecimal firstDayRevenue = dailyData.get(0)[1] != null ?
                        new BigDecimal(dailyData.get(0)[1].toString()) : BigDecimal.ZERO;
                BigDecimal lastDayRevenue = dailyData.get(dailyData.size()-1)[1] != null ?
                        new BigDecimal(dailyData.get(dailyData.size()-1)[1].toString()) : BigDecimal.ZERO;

                if (firstDayRevenue.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal growth = lastDayRevenue.subtract(firstDayRevenue)
                            .divide(firstDayRevenue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    trends.put("growthPercentage", growth);
                }
            } catch (Exception e) {
                System.err.println("Error calculating growth percentage: " + e.getMessage());
            }
        }

        return trends;
    }

    @Override
    public List<Map<String, Object>> getRevenueByCategory(AnalyticsFilter filter) {
        DateRange dateRange = getDateRangeFromFilter(filter);
        LocalDateTime startDateTime = dateRange.getStartDateTime();
        LocalDateTime endDateTime = dateRange.getEndDateTime();

        try {
            List<Object[]> categoryData = orderItemRepository.getRevenueByCategory(startDateTime, endDateTime);

            return categoryData.stream()
                    .map(data -> {
                        Map<String, Object> category = new HashMap<>();
                        category.put("category", data[0]);
                        category.put("revenue", data[1] != null ? new BigDecimal(data[1].toString()) : BigDecimal.ZERO);
                        category.put("orders", data[2]);
                        return category;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting revenue by category: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getRevenueByBrand(AnalyticsFilter filter) {
        DateRange dateRange = getDateRangeFromFilter(filter);
        LocalDateTime startDateTime = dateRange.getStartDateTime();
        LocalDateTime endDateTime = dateRange.getEndDateTime();

        try {
            List<Object[]> brandData = orderItemRepository.getRevenueByBrand(startDateTime, endDateTime);

            return brandData.stream()
                    .map(data -> {
                        Map<String, Object> brand = new HashMap<>();
                        brand.put("brand", data[0]);
                        brand.put("revenue", data[1] != null ? new BigDecimal(data[1].toString()) : BigDecimal.ZERO);
                        brand.put("orders", data[2]);
                        return brand;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting revenue by brand: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public CustomerAnalyticsDTO getCustomerAnalytics(AnalyticsFilter filter) {
        DateRange dateRange = getDateRangeFromFilter(filter);
        LocalDateTime startDateTime = dateRange.getStartDateTime();
        LocalDateTime endDateTime = dateRange.getEndDateTime();

        CustomerAnalyticsDTO response = new CustomerAnalyticsDTO();

        // Get total orders in period
        Long periodOrders = orderRepository.countByCreatedAtBetween(startDateTime, endDateTime);
        response.setPeriodOrders(periodOrders != null ? periodOrders : 0L);

        try {
            // Get unique customers from orders (based on phone)
            Long uniqueCustomers = orderRepository.countUniqueCustomers();
            response.setUniqueCustomers(uniqueCustomers != null ? uniqueCustomers : 0L);

            // Calculate average orders per customer
            if (uniqueCustomers != null && uniqueCustomers > 0 && periodOrders != null && periodOrders > 0) {
                BigDecimal avgOrdersPerCustomer = BigDecimal.valueOf(periodOrders)
                        .divide(BigDecimal.valueOf(uniqueCustomers), 2, RoundingMode.HALF_UP);
                response.setAvgOrdersPerCustomer(avgOrdersPerCustomer);
            } else {
                response.setAvgOrdersPerCustomer(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            System.err.println("Error calculating customer analytics: " + e.getMessage());
            response.setUniqueCustomers(0L);
            response.setAvgOrdersPerCustomer(BigDecimal.ZERO);
        }

        // Get top wilayas
        try {
           
            List<Object[]> wilayaData = orderRepository.getTopWilayasByDateRange(startDateTime, endDateTime, 5);
            List<CustomerAnalyticsDTO.WilayaData> topWilayas = wilayaData.stream()
                    .map(data -> {
                        CustomerAnalyticsDTO.WilayaData wilayaDataObj = new CustomerAnalyticsDTO.WilayaData();
                        wilayaDataObj.setWilaya((String) data[0]);
                        wilayaDataObj.setOrders(((Number) data[1]).longValue());
                        return wilayaDataObj;
                    })
                    .collect(Collectors.toList());

            response.setTopWilayas(topWilayas);

        } catch (Exception e) {
            System.err.println("Error getting top wilayas: " + e.getMessage());
            response.setTopWilayas(new ArrayList<>());
        }

        return response;
    }

    @Override
    public Map<String, Object> getPrescriptionAnalytics(AnalyticsFilter filter) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("message", "Prescription analytics requires adding prescription data to OrderItem");
        return analytics;
    }

    @Override
    public List<Map<String, Object>> getTopProducts(AnalyticsFilter filter) {
        DateRange dateRange = getDateRangeFromFilter(filter);
        LocalDateTime start = dateRange.getStartDateTime();
        LocalDateTime end = dateRange.getEndDateTime();

        try {
            List<Object[]> topProducts = getTopGlassesByPeriod(start, end, filter.getLimit());

            return topProducts.stream()
                    .map(data -> {
                        Map<String, Object> product = new HashMap<>();
                        product.put("id", data[0]);
                        product.put("name", data[1]);
                        product.put("brand", data[2]);
                        product.put("unitsSold", data[4]);
                        product.put("revenue", data[5] != null ? new BigDecimal(data[5].toString()) : BigDecimal.ZERO);
                        return product;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting top products: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getLowStockProducts(int threshold) {
        try {
            List<Glasses> allGlasses = glassesRepository.findByActiveTrue();

            return allGlasses.stream()
                    .filter(glass -> glass.getQuantity() != null && glass.getQuantity() <= threshold)
                    .map(glass -> {
                        Map<String, Object> product = new HashMap<>();
                        product.put("id", glass.getId());
                        product.put("name", glass.getName());
                        product.put("brand", glass.getBrand() != null ? glass.getBrand().getName() : "N/A");
                        product.put("quantity", glass.getQuantity());
                        product.put("price", glass.getPrice());
                        product.put("reorderLevel", threshold);
                        product.put("inStock", glass.isInStock());
                        return product;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting low stock products: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getInventoryTurnover() {
        Map<String, Object> turnover = new HashMap<>();

        try {
            long totalProducts = glassesRepository.count();
            turnover.put("totalProducts", totalProducts);

            List<Glasses> activeGlasses = glassesRepository.findByActiveTrue();
            turnover.put("activeProducts", activeGlasses.size());

            // Calculate total inventory value
            BigDecimal totalInventoryValue = activeGlasses.stream()
                    .filter(g -> g.getPrice() != null && g.getQuantity() != null)
                    .map(g -> g.getPrice().multiply(BigDecimal.valueOf(g.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            turnover.put("totalInventoryValue", totalInventoryValue);

        } catch (Exception e) {
            System.err.println("Error calculating inventory turnover: " + e.getMessage());
        }

        return turnover;
    }

    @Override
    public List<Map<String, Object>> getCustomerSegments() {
        List<Map<String, Object>> segments = new ArrayList<>();

        try {
            // Segment 1: Customers with multiple orders
            Map<String, Object> loyalCustomers = new HashMap<>();
            loyalCustomers.put("segment", "Loyal Customers");
            loyalCustomers.put("description", "Customers with 2+ orders");

            // Get count of customers with multiple orders
            Long multiOrderCustomers = orderRepository.countCustomersWithMultipleOrders();
            loyalCustomers.put("count", multiOrderCustomers != null ? multiOrderCustomers : 0);

            // Segment 2: High-value customers
            Map<String, Object> highValue = new HashMap<>();
            highValue.put("segment", "High-Value Customers");
            highValue.put("description", "Average order value > 50000 DZD");

            // Get average order value per customer
            BigDecimal avgOrderValue = orderRepository.getAverageOrderValuePerCustomer();
            highValue.put("avgOrderValue", avgOrderValue != null ? avgOrderValue : BigDecimal.ZERO);

            segments.add(loyalCustomers);
            segments.add(highValue);

        } catch (Exception e) {
            System.err.println("Error getting customer segments: " + e.getMessage());
        }

        return segments;
    }

    @Override
    public Map<String, Object> getCustomerRetention(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> retention = new HashMap<>();
        retention.put("period", startDate + " to " + endDate);
        retention.put("message", "Customer retention calculation requires customer tracking");
        return retention;
    }

    @Override
    public List<Map<String, Object>> getCustomerAcquisitionSources() {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getPrescriptionTrends() {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getLensTypePopularity() {
        return new HashMap<>();
    }

    @Override
    public byte[] exportAnalyticsReport(AnalyticsRequestDTO request, String format) {
        return "Analytics Report - Coming Soon".getBytes();
    }

    @Override
    public void refreshAnalyticsData() {
        System.out.println("Analytics data refresh triggered");
    }

    // Helper methods for date ranges
    private DateRange getDateRange(AnalyticsRequestDTO request) {
        if (request == null) {
            return new DateRange(LocalDate.now().minusMonths(1), LocalDate.now());
        }

        if (request.getPeriod() != TimePeriod.CUSTOM &&
                request.getStartDate() == null && request.getEndDate() == null) {
            return calculateDateRange(request.getPeriod());
        }
        return new DateRange(
                request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusMonths(1),
                request.getEndDate() != null ? request.getEndDate() : LocalDate.now()
        );
    }

    private DateRange getDateRangeFromFilter(AnalyticsFilter filter) {
        if (filter == null) {
            return new DateRange(LocalDate.now().minusMonths(1), LocalDate.now());
        }

        if (filter.getPeriod() != TimePeriod.CUSTOM &&
                filter.getStartDate() == null && filter.getEndDate() == null) {
            return calculateDateRange(filter.getPeriod());
        }
        return new DateRange(
                filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusMonths(1),
                filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now()
        );
    }

    private DateRange calculateDateRange(TimePeriod period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        if (period == null) {
            period = TimePeriod.THIS_MONTH;
        }

        switch (period) {
            case TODAY:
                startDate = endDate;
                break;
            case YESTERDAY:
                startDate = endDate.minusDays(1);
                endDate = endDate.minusDays(1);
                break;
            case THIS_WEEK:
                startDate = endDate.with(java.time.DayOfWeek.MONDAY);
                break;
            case LAST_WEEK:
                startDate = endDate.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
                endDate = startDate.plusDays(6);
                break;
            case THIS_MONTH:
                startDate = endDate.withDayOfMonth(1);
                break;
            case LAST_MONTH:
                startDate = endDate.minusMonths(1).withDayOfMonth(1);
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                break;
            case THIS_QUARTER:
                int currentQuarter = (endDate.getMonthValue() - 1) / 3 + 1;
                startDate = LocalDate.of(endDate.getYear(), (currentQuarter - 1) * 3 + 1, 1);
                break;
            case THIS_YEAR:
                startDate = LocalDate.of(endDate.getYear(), 1, 1);
                break;
            default:
                startDate = endDate.minusMonths(1);
        }

        return new DateRange(startDate, endDate);
    }

    // DateRange inner class
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DateRange {
        private LocalDate startDate;
        private LocalDate endDate;

        public DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDateTime getStartDateTime() {
            return startDate != null ? startDate.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        }

        public LocalDateTime getEndDateTime() {
            return endDate != null ? endDate.plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();
        }
    }
}