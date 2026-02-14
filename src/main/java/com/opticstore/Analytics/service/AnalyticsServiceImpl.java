package com.opticstore.Analytics.service;

import com.opticstore.Analytics.dto.AnalyticsRequestDTO;
import com.opticstore.Analytics.dto.CustomerAnalyticsDTO;
import com.opticstore.Analytics.dto.ProductPerformanceDTO;
import com.opticstore.Analytics.dto.RevenueAnalyticsDTO;
import com.opticstore.Analytics.model.AnalyticsFilter;
import com.opticstore.Analytics.model.TimePeriod;
import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderStatus;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

        String category = request.getCategory();
        String brand = request.getBrand();

        System.out.println("=== Processing Revenue Analytics ===");
        System.out.println("Start: " + startDateTime);
        System.out.println("End: " + endDateTime);
        System.out.println("Category: " + category);
        System.out.println("Brand: " + brand);

        // ================================
        // TOTAL REVENUE WITH FILTERS
        // ================================
        BigDecimal totalRevenue;
        if ((category != null && !category.isEmpty()) || (brand != null && !brand.isEmpty())) {
            System.out.println("Using filtered revenue query");
            totalRevenue = orderItemRepository.getFilteredRevenue(startDateTime, endDateTime, category, brand);
        } else {
            System.out.println("Using unfiltered revenue query");
            totalRevenue = orderRepository.sumRevenueBetweenDates(startDateTime, endDateTime);
        }
        System.out.println("Total Revenue: " + totalRevenue);
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // ====================================
        // DELIVERED ORDER COUNT WITH FILTERS
        // ====================================
        Long deliveredOrderCount;
        if ((category != null && !category.isEmpty()) || (brand != null && !brand.isEmpty())) {
            deliveredOrderCount = orderItemRepository.getFilteredOrdersCount(startDateTime, endDateTime, category, brand);
        } else {
            deliveredOrderCount = orderRepository.countDeliveredOrdersBetweenDates(startDateTime, endDateTime);
        }
        System.out.println("Delivered Orders: " + deliveredOrderCount);
        response.setTotalOrders(deliveredOrderCount != null ? deliveredOrderCount : 0L);

        // ====================================
        // AVERAGE ORDER VALUE
        // ====================================
        if (deliveredOrderCount != null && deliveredOrderCount > 0 &&
                totalRevenue != null && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal avgOrderValue = totalRevenue.divide(
                    BigDecimal.valueOf(deliveredOrderCount),
                    2,
                    RoundingMode.HALF_UP
            );
            response.setAverageOrderValue(avgOrderValue);
        } else {
            response.setAverageOrderValue(BigDecimal.ZERO);
        }

        // ====================================
        // CONVERSION RATE
        // ====================================
        Long totalOrdersInPeriod;
        if ((category != null && !category.isEmpty()) || (brand != null && !brand.isEmpty())) {
            // Get total orders (including non-delivered) with filters
            totalOrdersInPeriod = orderItemRepository.getTotalOrdersWithFilters(startDateTime, endDateTime, category, brand);
        } else {
            totalOrdersInPeriod = orderRepository.countByCreatedAtBetween(startDateTime, endDateTime);
        }
        System.out.println("Total Orders (including non-delivered): " + totalOrdersInPeriod);

        if (totalOrdersInPeriod != null && totalOrdersInPeriod > 0 &&
                deliveredOrderCount != null && deliveredOrderCount > 0) {
            BigDecimal conversionRate = BigDecimal.valueOf(deliveredOrderCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOrdersInPeriod), 2, RoundingMode.HALF_UP);
            response.setConversionRate(conversionRate);
        } else {
            response.setConversionRate(BigDecimal.ZERO);
        }

        // ====================================
        // DAILY REVENUE TRENDS WITH FILTERS
        // ====================================
        List<Object[]> dailyRevenueData;
        if ((category != null && !category.isEmpty()) || (brand != null && !brand.isEmpty())) {
            dailyRevenueData = orderItemRepository.getDailyRevenueWithFilters(startDateTime, endDateTime, category, brand);
        } else {
            dailyRevenueData = orderRepository.getDailyRevenueBetweenDates(startDateTime, endDateTime);
        }
        System.out.println("Daily Revenue Data Points: " + (dailyRevenueData != null ? dailyRevenueData.size() : 0));

        List<RevenueAnalyticsDTO.TimeSeriesData> dailyRevenue = new ArrayList<>();

        // Process dailyRevenueData
        if (dailyRevenueData != null) {
            for (Object[] data : dailyRevenueData) {
                try {
                    RevenueAnalyticsDTO.TimeSeriesData tsData = new RevenueAnalyticsDTO.TimeSeriesData();

                    // Handle date (for native query results)
                    if (data[0] instanceof java.sql.Date) {
                        tsData.setDate(((java.sql.Date) data[0]).toLocalDate());
                    } else if (data[0] instanceof java.sql.Timestamp) {
                        tsData.setDate(((java.sql.Timestamp) data[0]).toLocalDateTime().toLocalDate());
                    } else if (data[0] instanceof LocalDate) {
                        tsData.setDate((LocalDate) data[0]);
                    }

                    // Handle revenue
                    if (data[1] != null) {
                        if (data[1] instanceof BigDecimal) {
                            tsData.setValue((BigDecimal) data[1]);
                        } else {
                            tsData.setValue(new BigDecimal(data[1].toString()));
                        }
                    } else {
                        tsData.setValue(BigDecimal.ZERO);
                    }

                    // Handle order count
                    if (data.length > 2 && data[2] != null) {
                        tsData.setOrders(((Number) data[2]).intValue());
                    } else {
                        tsData.setOrders(0);
                    }

                    dailyRevenue.add(tsData);
                } catch (Exception e) {
                    System.err.println("Error processing daily revenue data: " + e.getMessage());
                }
            }
        }

        response.setDailyRevenue(dailyRevenue);

        // ====================================
        // REVENUE BY CATEGORY WITH FILTERS
        // ====================================
        try {
            Map<String, BigDecimal> revenueByCategory;
            if (category != null && !category.isEmpty()) {
                // If filtering by category, get data for that specific category
                revenueByCategory = getRevenueByCategory(startDateTime, endDateTime, category);
            } else {
                // No category filter - get all categories (optionally filtered by brand)
                if (brand != null && !brand.isEmpty()) {
                    revenueByCategory = getRevenueByCategoryWithBrandFilter(startDateTime, endDateTime, brand);
                } else {
                    revenueByCategory = getRevenueByCategory(startDateTime, endDateTime);
                }
            }
            System.out.println("Revenue by Category: " + revenueByCategory);
            response.setRevenueByCategory(revenueByCategory);
        } catch (Exception e) {
            System.err.println("Error in revenue by category: " + e.getMessage());
            response.setRevenueByCategory(new HashMap<>());
        }

        // ====================================
        // REVENUE BY BRAND WITH FILTERS
        // ====================================
        try {
            Map<String, BigDecimal> revenueByBrand;
            if (brand != null && !brand.isEmpty()) {
                // If filtering by brand, get data for that specific brand
                revenueByBrand = getRevenueByBrand(startDateTime, endDateTime, brand);
            } else {
                // No brand filter - get all brands (optionally filtered by category)
                if (category != null && !category.isEmpty()) {
                    revenueByBrand = getRevenueByBrandWithCategoryFilter(startDateTime, endDateTime, category);
                } else {
                    revenueByBrand = getRevenueByBrand(startDateTime, endDateTime);
                }
            }
            System.out.println("Revenue by Brand: " + revenueByBrand);
            response.setRevenueByBrand(revenueByBrand);
        } catch (Exception e) {
            System.err.println("Error in revenue by brand: " + e.getMessage());
            response.setRevenueByBrand(new HashMap<>());
        }

        return response;
    }

    // ==================== HELPER METHODS FOR REVENUE BY CATEGORY/BRAND ====================

    private Map<String, BigDecimal> getRevenueByCategory(LocalDateTime start, LocalDateTime end) {
        return getRevenueByCategory(start, end, null);
    }

    private Map<String, BigDecimal> getRevenueByCategory(LocalDateTime start, LocalDateTime end, String categoryFilter) {
        try {
            List<Object[]> categoryData;
            if (categoryFilter != null && !categoryFilter.isEmpty()) {
                categoryData = orderItemRepository.getRevenueByCategoryWithFilter(start, end, categoryFilter);
            } else {
                categoryData = orderItemRepository.getRevenueByCategory(start, end);
            }

            Map<String, BigDecimal> result = new LinkedHashMap<>();

            for (Object[] data : categoryData) {
                if (data[0] != null && data[1] != null) {
                    String category = data[0].toString();
                    BigDecimal revenue;

                    if (data[1] instanceof BigDecimal) {
                        revenue = (BigDecimal) data[1];
                    } else {
                        revenue = new BigDecimal(data[1].toString());
                    }

                    result.put(category, revenue);
                }
            }

            return result;
        } catch (Exception e) {
            System.err.println("Error getting revenue by category: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<String, BigDecimal> getRevenueByCategoryWithBrandFilter(LocalDateTime start, LocalDateTime end, String brand) {
        try {
            List<Object[]> categoryData = orderItemRepository.getRevenueByCategoryWithBrandFilter(start, end, brand);

            Map<String, BigDecimal> result = new LinkedHashMap<>();

            for (Object[] data : categoryData) {
                if (data[0] != null && data[1] != null) {
                    String category = data[0].toString();
                    BigDecimal revenue;

                    if (data[1] instanceof BigDecimal) {
                        revenue = (BigDecimal) data[1];
                    } else {
                        revenue = new BigDecimal(data[1].toString());
                    }

                    result.put(category, revenue);
                }
            }

            return result;
        } catch (Exception e) {
            System.err.println("Error getting revenue by category with brand filter: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<String, BigDecimal> getRevenueByBrand(LocalDateTime start, LocalDateTime end) {
        return getRevenueByBrand(start, end, null);
    }

    private Map<String, BigDecimal> getRevenueByBrand(LocalDateTime start, LocalDateTime end, String brandFilter) {
        try {
            List<Object[]> brandData;
            if (brandFilter != null && !brandFilter.isEmpty()) {
                brandData = orderItemRepository.getRevenueByBrandWithFilter(start, end, brandFilter);
            } else {
                brandData = orderItemRepository.getRevenueByBrand(start, end);
            }

            Map<String, BigDecimal> result = new LinkedHashMap<>();

            for (Object[] data : brandData) {
                if (data[0] != null && data[1] != null) {
                    String brand = data[0].toString();
                    BigDecimal revenue;

                    if (data[1] instanceof BigDecimal) {
                        revenue = (BigDecimal) data[1];
                    } else {
                        revenue = new BigDecimal(data[1].toString());
                    }

                    result.put(brand, revenue);
                }
            }

            return result;
        } catch (Exception e) {
            System.err.println("Error getting revenue by brand: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<String, BigDecimal> getRevenueByBrandWithCategoryFilter(LocalDateTime start, LocalDateTime end, String category) {
        try {
            List<Object[]> brandData = orderItemRepository.getRevenueByBrandWithCategoryFilter(start, end, category);

            Map<String, BigDecimal> result = new LinkedHashMap<>();

            for (Object[] data : brandData) {
                if (data[0] != null && data[1] != null) {
                    String brand = data[0].toString();
                    BigDecimal revenue;

                    if (data[1] instanceof BigDecimal) {
                        revenue = (BigDecimal) data[1];
                    } else {
                        revenue = new BigDecimal(data[1].toString());
                    }

                    result.put(brand, revenue);
                }
            }

            return result;
        } catch (Exception e) {
            System.err.println("Error getting revenue by brand with category filter: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // ==================== TOP PRODUCTS METHODS ====================

    private List<Object[]> getTopGlassesByPeriod(LocalDateTime start, LocalDateTime end, int limit, String category, String brand) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            if ((category != null && !category.isEmpty()) || (brand != null && !brand.isEmpty())) {
                return orderItemRepository.getTopSellingGlassesWithFilters(start, end, category, brand, pageable);
            } else {
                return orderItemRepository.getTopSellingGlasses(start, end, pageable);
            }
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

        String category = filter.getCategory();
        String brand = filter.getBrand();

        // Get top selling glasses with filters
        List<ProductPerformanceDTO.ProductStats> topProducts = new ArrayList<>();

        try {
            List<Object[]> topGlassesData = getTopGlassesByPeriod(startDateTime, endDateTime,
                    filter.getLimit() != null ? filter.getLimit() : 10, category, brand);

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
                        if (data[5] instanceof BigDecimal) {
                            stats.setRevenue((BigDecimal) data[5]);
                        } else {
                            stats.setRevenue(new BigDecimal(data[5].toString()));
                        }
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
        String brand = filter.getBrand();

        try {
            List<Object[]> categoryData;
            if (brand != null && !brand.isEmpty()) {
                categoryData = orderItemRepository.getRevenueByCategoryWithBrandFilter(startDateTime, endDateTime, brand);
            } else {
                categoryData = orderItemRepository.getRevenueByCategory(startDateTime, endDateTime);
            }

            return categoryData.stream()
                    .map(data -> {
                        Map<String, Object> category = new HashMap<>();
                        category.put("category", data[0]);
                        category.put("revenue", data[1] != null ? new BigDecimal(data[1].toString()) : BigDecimal.ZERO);
                        category.put("orders", data[2] != null ? data[2] : 0);
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
        String category = filter.getCategory();

        try {
            List<Object[]> brandData;
            if (category != null && !category.isEmpty()) {
                brandData = orderItemRepository.getRevenueByBrandWithCategoryFilter(startDateTime, endDateTime, category);
            } else {
                brandData = orderItemRepository.getRevenueByBrand(startDateTime, endDateTime);
            }

            return brandData.stream()
                    .map(data -> {
                        Map<String, Object> brand = new HashMap<>();
                        brand.put("brand", data[0]);
                        brand.put("revenue", data[1] != null ? new BigDecimal(data[1].toString()) : BigDecimal.ZERO);
                        brand.put("orders", data[2] != null ? data[2] : 0);
                        return brand;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting revenue by brand: " + e.getMessage());
            return new ArrayList<>();
        }
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
        String category = filter.getCategory();
        String brand = filter.getBrand();

        try {
            List<Object[]> topProducts = getTopGlassesByPeriod(start, end,
                    filter.getLimit() != null ? filter.getLimit() : 10, category, brand);

            return topProducts.stream()
                    .map(data -> {
                        Map<String, Object> product = new HashMap<>();
                        product.put("id", data[0]);
                        product.put("name", data[1]);
                        product.put("brand", data[2]);
                        product.put("category", data[3]);
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
                        product.put("category", glass.getCategory() != null ? glass.getCategory().getName() : "N/A");
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
    public CustomerAnalyticsDTO getCustomerAnalytics(AnalyticsFilter filter) {
        DateRange dateRange = getDateRangeFromFilter(filter);
        LocalDateTime startDateTime = dateRange.getStartDateTime();
        LocalDateTime endDateTime = dateRange.getEndDateTime();

        String category = filter.getCategory();
        String brand = filter.getBrand();

        CustomerAnalyticsDTO response = new CustomerAnalyticsDTO();

        try {
            // ================================
            // 1. DELIVERED ORDERS COUNT WITH FILTERS
            // ================================
            Long periodOrders;
            if ((category != null && !category.isEmpty()) || (brand != null && !brand.isEmpty())) {
                periodOrders = orderItemRepository.getFilteredOrdersCount(startDateTime, endDateTime, category, brand);
            } else {
                periodOrders = orderRepository.countDeliveredOrdersBetweenDates(startDateTime, endDateTime);
            }
            response.setPeriodOrders(periodOrders != null ? periodOrders : 0L);

            // ================================
            // 2. UNIQUE CUSTOMERS (DELIVERED) WITH FILTERS
            // ================================
            Long uniqueCustomers;
            if ((category != null && !category.isEmpty()) || (brand != null && !brand.isEmpty())) {
                uniqueCustomers = orderItemRepository.getFilteredUniqueCustomers(startDateTime, endDateTime, category, brand);
            } else {
                uniqueCustomers = orderRepository.countUniqueCustomersWithDeliveredOrdersByDateRange(
                        startDateTime, endDateTime
                );
            }
            response.setUniqueCustomers(uniqueCustomers != null ? uniqueCustomers : 0L);

            // ================================
            // 3. AVERAGE ORDERS PER CUSTOMER
            // ================================
            if (uniqueCustomers != null && uniqueCustomers > 0 && periodOrders != null && periodOrders > 0) {
                BigDecimal avgOrdersPerCustomer = BigDecimal.valueOf(periodOrders)
                        .divide(BigDecimal.valueOf(uniqueCustomers), 2, RoundingMode.HALF_UP);
                response.setAvgOrdersPerCustomer(avgOrdersPerCustomer);
            } else {
                response.setAvgOrdersPerCustomer(BigDecimal.ZERO);
            }

            // ================================
            // 4. TOP WILAYAS WITH DETAILS AND FILTERS
            // ================================
            List<Object[]> wilayaData;
            if ((category != null && !category.isEmpty()) || (brand != null && !brand.isEmpty())) {
                wilayaData = orderItemRepository.getTopWilayasWithFilters(startDateTime, endDateTime, category, brand, 5);
            } else {
                wilayaData = orderRepository.getTopWilayasWithDetails(startDateTime, endDateTime, 5);
            }

            List<CustomerAnalyticsDTO.WilayaData> topWilayas = new ArrayList<>();

            for (Object[] data : wilayaData) {
                if (data[0] != null && data[0].toString() != null && !data[0].toString().isEmpty()) {
                    CustomerAnalyticsDTO.WilayaData wilaya = new CustomerAnalyticsDTO.WilayaData();

                    // Wilaya name
                    wilaya.setWilaya(data[0].toString());

                    // Order count
                    if (data[1] != null) {
                        wilaya.setOrders(((Number) data[1]).longValue());
                    } else {
                        wilaya.setOrders(0L);
                    }

                    // Revenue
                    if (data[2] != null) {
                        if (data[2] instanceof BigDecimal) {
                            wilaya.setRevenue((BigDecimal) data[2]);
                        } else {
                            wilaya.setRevenue(new BigDecimal(data[2].toString()));
                        }
                    } else {
                        wilaya.setRevenue(BigDecimal.ZERO);
                    }

                    // Customer count
                    if (data.length > 3 && data[3] != null) {
                        wilaya.setCustomers(((Number) data[3]).longValue());
                    } else {
                        wilaya.setCustomers(0L);
                    }

                    topWilayas.add(wilaya);
                }
            }

            response.setTopWilayas(topWilayas);

            // ================================
            // 5. CUSTOMER GROWTH
            // ================================
            try {
                // Calculate previous period for growth
                long daysBetween = ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
                LocalDateTime previousStart = startDateTime.minusDays(daysBetween);
                LocalDateTime previousEnd = startDateTime;

                Long previousCustomers;
                if ((category != null && !category.isEmpty()) || (brand != null && !brand.isEmpty())) {
                    previousCustomers = orderItemRepository.getFilteredUniqueCustomers(previousStart, previousEnd, category, brand);
                } else {
                    previousCustomers = orderRepository.countUniqueCustomersWithDeliveredOrdersByDateRange(
                            previousStart, previousEnd
                    );
                }

                if (previousCustomers != null && previousCustomers > 0 && uniqueCustomers != null) {
                    BigDecimal growth = BigDecimal.valueOf(uniqueCustomers - previousCustomers)
                            .divide(BigDecimal.valueOf(previousCustomers), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    response.setCustomerGrowth(growth);
                } else {
                    response.setCustomerGrowth(BigDecimal.ZERO);
                }
            } catch (Exception e) {
                System.err.println("Error calculating customer growth: " + e.getMessage());
                response.setCustomerGrowth(BigDecimal.ZERO);
            }

        } catch (Exception e) {
            System.err.println("Error calculating customer analytics: " + e.getMessage());
            e.printStackTrace();

            // Set default values on error
            response.setPeriodOrders(0L);
            response.setUniqueCustomers(0L);
            response.setAvgOrdersPerCustomer(BigDecimal.ZERO);
            response.setTopWilayas(new ArrayList<>());
            response.setCustomerGrowth(BigDecimal.ZERO);
        }

        return response;
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

    // ==================== DAILY REVENUE DATA HELPER ====================

    private List<Object[]> getDailyRevenueData(LocalDateTime start, LocalDateTime end) {
        try {
            return orderRepository.getDailyRevenueBetweenDates(start, end);
        } catch (Exception e) {
            System.err.println("Error in getDailyRevenueData: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== DATE RANGE HELPER METHODS ====================

    private DateRange getDateRange(AnalyticsRequestDTO request) {
        if (request == null) {
            return new DateRange(LocalDate.now().minusMonths(1), LocalDate.now());
        }

        // Handle CUSTOM period
        if (request.getPeriod() == TimePeriod.CUSTOM) {
            // Validate custom dates
            if (request.getStartDate() != null && request.getEndDate() != null) {
                // Ensure end date is not before start date
                if (request.getEndDate().isBefore(request.getStartDate())) {
                    // Swap dates if they're in wrong order
                    return new DateRange(request.getEndDate(), request.getStartDate());
                }
                return new DateRange(request.getStartDate(), request.getEndDate());
            } else {
                // If CUSTOM period but dates not provided, default to last 30 days
                System.out.println("CUSTOM period selected but dates not provided. Defaulting to last 30 days.");
                return new DateRange(LocalDate.now().minusDays(30), LocalDate.now());
            }
        }

        // Handle all other pre-defined periods
        return calculateDateRange(request.getPeriod());
    }

    private DateRange getDateRangeFromFilter(AnalyticsFilter filter) {
        if (filter == null) {
            return new DateRange(LocalDate.now().minusMonths(1), LocalDate.now());
        }

        // Handle CUSTOM period specifically
        if (filter.getPeriod() == TimePeriod.CUSTOM) {
            if (filter.getStartDate() != null && filter.getEndDate() != null) {
                return new DateRange(filter.getStartDate(), filter.getEndDate());
            } else {
                // If CUSTOM but dates are null, default to last 30 days
                return new DateRange(LocalDate.now().minusDays(30), LocalDate.now());
            }
        }

        // Handle pre-defined periods
        return calculateDateRange(filter.getPeriod());
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

    // ==================== INNER CLASS ====================

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