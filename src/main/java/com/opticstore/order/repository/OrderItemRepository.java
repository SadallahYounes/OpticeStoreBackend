package com.opticstore.order.repository;

import com.opticstore.order.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Get revenue by category - ONLY DELIVERED ORDERS
    @Query(value = "SELECT c.name as category, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue, " +
            "COUNT(oi.id) as order_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "JOIN category c ON g.category_id = c.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +  // ADD THIS FILTER
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByCategory(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);


    // Get revenue by brand - ONLY DELIVERED ORDERS
    @Query(value = "SELECT b.name as brand, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as revenue, " +
            "COUNT(oi.id) as order_count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "JOIN brand b ON g.brand_id = b.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "GROUP BY b.name " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByBrand(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    // Get top selling glasses
    @Query(value = "SELECT g.id, g.name, b.name as brand, c.name as category, " +
            "SUM(oi.quantity) as units_sold, " +
            "SUM(oi.price * oi.quantity) as revenue " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN glasses g ON oi.glass_id = g.id " +
            "LEFT JOIN brand b ON g.brand_id = b.id " +
            "LEFT JOIN category c ON g.category_id = c.id " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "GROUP BY g.id, g.name, b.name, c.name " +
            "ORDER BY revenue DESC " +
            "LIMIT :#{#pageable.pageSize}",
            nativeQuery = true)
    List<Object[]> getTopSellingGlasses(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        Pageable pageable);
}