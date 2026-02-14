package com.opticstore.order.repository;

import com.opticstore.order.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // ==================== REVENUE BY CATEGORY METHODS ====================

    // Get revenue by category - ALL DELIVERED ORDERS
    @Query(value = "SELECT c.name as category, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue, " +
            "COUNT(DISTINCT o.id) as order_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "JOIN categories c ON g.category_id = c.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "GROUP BY c.name " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByCategory(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    // Get revenue by category with optional category filter
    @Query(value = "SELECT c.name as category, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue, " +
            "COUNT(DISTINCT o.id) as order_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "JOIN categories c ON g.category_id = c.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:category IS NULL OR c.name = :category) " +
            "GROUP BY c.name " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByCategoryWithFilter(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category
    );

    // ==================== REVENUE BY BRAND METHODS ====================

    // Get revenue by brand - ALL DELIVERED ORDERS
    @Query(value = "SELECT b.name as brand, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue, " +
            "COUNT(DISTINCT o.id) as order_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "JOIN brands b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "GROUP BY b.name " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByBrand(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    // Get revenue by brand with optional brand filter
    @Query(value = "SELECT b.name as brand, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue, " +
            "COUNT(DISTINCT o.id) as order_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "JOIN brands b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:brand IS NULL OR b.name = :brand) " +
            "GROUP BY b.name " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByBrandWithFilter(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("brand") String brand
    );

    // ==================== TOP SELLING GLASSES METHODS ====================

    // Get top selling glasses - ALL DELIVERED ORDERS
    @Query(value = "SELECT g.id, g.name, b.name as brand, c.name as category, " +
            "COALESCE(SUM(oi.quantity), 0) as units_sold, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "LEFT JOIN brands b ON g.brand_id = b.id " +
            "LEFT JOIN categories c ON g.category_id = c.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "GROUP BY g.id, g.name, b.name, c.name " +
            "ORDER BY revenue DESC " +
            "LIMIT :#{#pageable.pageSize}",
            nativeQuery = true)
    List<Object[]> getTopSellingGlasses(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        Pageable pageable);

    // Get top selling glasses with category and/or brand filters
    @Query(value = "SELECT g.id, g.name, b.name as brand, c.name as category, " +
            "COALESCE(SUM(oi.quantity), 0) as units_sold, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "LEFT JOIN brands b ON g.brand_id = b.id " +
            "LEFT JOIN categories c ON g.category_id = c.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:category IS NULL OR c.name = :category) " +
            "AND (:brand IS NULL OR b.name = :brand) " +
            "GROUP BY g.id, g.name, b.name, c.name " +
            "ORDER BY revenue DESC " +
            "LIMIT :#{#pageable.pageSize}",
            nativeQuery = true)
    List<Object[]> getTopSellingGlassesWithFilters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category,
            @Param("brand") String brand,
            Pageable pageable
    );

    // ==================== DAILY REVENUE METHODS ====================

    // Get daily revenue with category and/or brand filters
    @Query(value = "SELECT DATE(o.created_at) as date, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue, " +
            "COUNT(DISTINCT o.id) as order_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "LEFT JOIN categories c ON g.category_id = c.id " +
            "LEFT JOIN brands b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:category IS NULL OR c.name = :category) " +
            "AND (:brand IS NULL OR b.name = :brand) " +
            "GROUP BY DATE(o.created_at) " +
            "ORDER BY DATE(o.created_at)",
            nativeQuery = true)
    List<Object[]> getDailyRevenueWithFilters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category,
            @Param("brand") String brand
    );

    // ==================== FILTERED COUNTS AND SUMS ====================

    // Get filtered orders count by category and/or brand
    @Query(value = "SELECT COUNT(DISTINCT o.id) " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "LEFT JOIN categories c ON g.category_id = c.id " +
            "LEFT JOIN brands b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:category IS NULL OR c.name = :category) " +
            "AND (:brand IS NULL OR b.name = :brand)",
            nativeQuery = true)
    Long getFilteredOrdersCount(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category,
            @Param("brand") String brand
    );

    // Get filtered total revenue by category and/or brand
    @Query(value = "SELECT COALESCE(SUM(oi.price * oi.quantity), 0) " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "LEFT JOIN categories c ON g.category_id = c.id " +
            "LEFT JOIN brands b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:category IS NULL OR c.name = :category) " +
            "AND (:brand IS NULL OR b.name = :brand)",
            nativeQuery = true)
    BigDecimal getFilteredRevenue(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category,
            @Param("brand") String brand
    );

    // ==================== ADDITIONAL USEFUL METHODS ====================

    // Get total units sold for a specific product in a date range
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE oi.glassId = :glassId " +
            "AND o.createdAt BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED'")
    Integer getTotalUnitsSoldForProduct(
            @Param("glassId") Long glassId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    // Get products that are out of stock (based on orders)
    @Query("SELECT g.id, g.name, COALESCE(SUM(oi.quantity), 0) as totalSold " +
            "FROM Glasses g " +
            "LEFT JOIN OrderItem oi ON oi.glassId = g.id " +
            "LEFT JOIN oi.order o ON o.createdAt BETWEEN :start AND :end AND o.status = 'DELIVERED' " +
            "WHERE g.quantity <= 0 " +
            "GROUP BY g.id, g.name")
    List<Object[]> getOutOfStockProductsWithSales(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );



    // Get total orders (including non-delivered) with category/brand filters
    @Query(value = "SELECT COUNT(DISTINCT o.id) " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "LEFT JOIN categories c ON g.category_id = c.id " +
            "LEFT JOIN brands b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND (:category IS NULL OR c.name = :category) " +
            "AND (:brand IS NULL OR b.name = :brand)",
            nativeQuery = true)
    Long getTotalOrdersWithFilters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category,
            @Param("brand") String brand
    );

    // Get revenue by category with brand filter
    @Query(value = "SELECT c.name as category, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue, " +
            "COUNT(DISTINCT o.id) as order_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "JOIN categories c ON g.category_id = c.id " +
            "LEFT JOIN brands b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:brand IS NULL OR b.name = :brand) " +
            "GROUP BY c.name " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByCategoryWithBrandFilter(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("brand") String brand
    );

    // Get revenue by brand with category filter
    @Query(value = "SELECT b.name as brand, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue, " +
            "COUNT(DISTINCT o.id) as order_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "JOIN brands b ON g.brand_id = b.id " +
            "LEFT JOIN categories c ON g.category_id = c.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:category IS NULL OR c.name = :category) " +
            "GROUP BY b.name " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByBrandWithCategoryFilter(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category
    );

    // Get filtered unique customers
    @Query(value = "SELECT COUNT(DISTINCT o.phone) " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "LEFT JOIN categories c ON g.category_id = c.id " +
            "LEFT JOIN brands b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:category IS NULL OR c.name = :category) " +
            "AND (:brand IS NULL OR b.name = :brand)",
            nativeQuery = true)
    Long getFilteredUniqueCustomers(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category,
            @Param("brand") String brand
    );

    // Get top wilayas with filters
    @Query(value = "SELECT o.wilaya, " +
            "COUNT(DISTINCT o.id) as order_count, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as total_revenue, " +
            "COUNT(DISTINCT o.phone) as customer_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "LEFT JOIN categories c ON g.category_id = c.id " +
            "LEFT JOIN brands b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "AND (:category IS NULL OR c.name = :category) " +
            "AND (:brand IS NULL OR b.name = :brand) " +
            "GROUP BY o.wilaya " +
            "ORDER BY order_count DESC, total_revenue DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> getTopWilayasWithFilters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category,
            @Param("brand") String brand,
            @Param("limit") int limit
    );
}