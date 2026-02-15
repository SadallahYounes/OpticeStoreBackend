package com.opticstore.order.repository;

import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT o FROM Order o
        WHERE
        (
            LOWER(o.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR o.phone LIKE CONCAT('%', :search, '%')
        )
        AND (:status IS NULL OR o.status = :status)
        AND (:wilaya IS NULL OR o.wilaya = :wilaya)
    """)
    Page<Order> searchAdminOrders(
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("wilaya") String wilaya,
            Pageable pageable
    );

    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o")
    long totalOrders();

    @Query("""
        SELECT COALESCE(SUM(o.total), 0)
        FROM Order o
        WHERE o.status = 'DELIVERED'
    """)
    BigDecimal totalRevenue();

    @Query("""
        SELECT DATE(o.createdAt), SUM(o.total)
        FROM Order o
        GROUP BY DATE(o.createdAt)
        ORDER BY DATE(o.createdAt)
    """)
    List<Object[]> salesByDay();

    // STATS METHODS
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByCreatedAtAfter(LocalDateTime date);

    // Count orders between dates
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :start AND :end")
    Long countDeliveredOrdersBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Order> findByStatus(OrderStatus status);

    // Sum revenue between dates - ONLY DELIVERED ORDERS (FIX EXISTING)
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :start AND :end")  // ADD STATUS FILTER
    BigDecimal sumRevenueBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Return BigDecimal instead of Double
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.createdAt >= :date")
    BigDecimal sumRevenueAfterDate(@Param("date") LocalDateTime date);

    // Add status filter to revenue queries
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end")
    BigDecimal sumRevenueByStatusBetweenDates(
            @Param("status") OrderStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = "SELECT COUNT(DISTINCT phone) FROM orders", nativeQuery = true)
    Long countUniqueCustomers();

    // count by unique phone + name combination
    @Query(value = "SELECT COUNT(DISTINCT CONCAT(first_name, ' ', last_name, ' ', phone)) FROM orders", nativeQuery = true)
    Long countUniqueCustomerIdentities();

    // ==== NEW ANALYTICS QUERIES (FIXED VERSIONS) ====

    // Get daily revenue with order count - USING NATIVE QUERY
    @Query(value = "SELECT DATE(o.created_at) as date, " +
            "COALESCE(SUM(o.total), 0) as revenue, " +
            "COUNT(o.id) as orders " +
            "FROM orders o " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +  // ADD THIS FILTER
            "GROUP BY DATE(o.created_at) " +
            "ORDER BY DATE(o.created_at)",
            nativeQuery = true)
    List<Object[]> getDailyRevenueBetweenDates(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    // Get monthly revenue - ONLY DELIVERED ORDERS
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-%m') as month, " +
            "COALESCE(SUM(o.total), 0) as revenue, " +
            "COUNT(o.id) as orders " +
            "FROM orders o " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +  // ADD THIS FILTER
            "GROUP BY DATE_FORMAT(o.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(o.created_at, '%Y-%m')",
            nativeQuery = true)
    List<Object[]> getMonthlyRevenueBetweenDates(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);


    // Get top wilayas - ONLY DELIVERED ORDERS
    @Query(value = "SELECT o.wilaya, COUNT(o.id) as order_count " +
            "FROM orders o " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +  // ADD THIS FILTER
            "GROUP BY o.wilaya " +
            "ORDER BY order_count DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> getTopWilayasByDateRange(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            @Param("limit") int limit);

    // Count customers with multiple orders - USING NATIVE QUERY
    @Query(value = "SELECT COUNT(*) FROM (" +
            "SELECT phone, COUNT(*) as order_count FROM orders " +
            "GROUP BY phone HAVING COUNT(*) > 1" +
            ") as multi_order_customers",
            nativeQuery = true)
    Long countCustomersWithMultipleOrders();

    // Average order value by customer - USING NATIVE QUERY
    @Query(value = "SELECT AVG(order_avg) FROM (" +
            "SELECT phone, AVG(total) as order_avg FROM orders " +
            "GROUP BY phone" +
            ") as customer_avg",
            nativeQuery = true)
    BigDecimal getAverageOrderValuePerCustomer();

    // Count unique customers with DELIVERED orders
    @Query(value = "SELECT COUNT(DISTINCT phone) FROM orders WHERE status = 'DELIVERED'", nativeQuery = true)
    Long countUniqueCustomersWithDeliveredOrders();

    // Count customers with multiple DELIVERED orders
    @Query(value = "SELECT COUNT(*) FROM (" +
            "SELECT phone, COUNT(*) as order_count FROM orders " +
            "WHERE status = 'DELIVERED' " +  // ADD FILTER
            "GROUP BY phone HAVING COUNT(*) > 1" +
            ") as multi_order_customers",
            nativeQuery = true)
    Long countCustomersWithMultipleDeliveredOrders();

    // Average order value by customer - ONLY DELIVERED ORDERS
    @Query(value = "SELECT AVG(order_avg) FROM (" +
            "SELECT phone, AVG(total) as order_avg FROM orders " +
            "WHERE status = 'DELIVERED' " +  // ADD FILTER
            "GROUP BY phone" +
            ") as customer_avg",
            nativeQuery = true)
    BigDecimal getAverageDeliveredOrderValuePerCustomer();


    // ================================
    //WILAYA ANALYTICS
    // ================================
    @Query(value = "SELECT COUNT(DISTINCT phone) FROM orders " +
            "WHERE status = 'DELIVERED' " +
            "AND created_at BETWEEN :start AND :end",
            nativeQuery = true)
    Long countUniqueCustomersWithDeliveredOrdersByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    // In OrderRepository.java - More efficient single query
    @Query(value = "SELECT " +
            "o.wilaya, " +
            "COUNT(o.id) as order_count, " +
            "COALESCE(SUM(o.total), 0) as total_revenue, " +
            "COUNT(DISTINCT o.phone) as customer_count " +
            "FROM orders o " +
            "WHERE o.created_at BETWEEN :start AND :end " +
            "AND o.status = 'DELIVERED' " +
            "GROUP BY o.wilaya " +
            "ORDER BY order_count DESC, total_revenue DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> getTopWilayasWithDetails(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("limit") int limit
    );

    // ==================== CUSTOMER QUERIES ====================
    // Get all unique customers with their stats - REMOVED email
    @Query(value = """
        SELECT 
            o.phone as phone,
            MAX(CONCAT(o.first_name, ' ', o.last_name)) as name,
            MAX(o.wilaya) as wilaya,
            MAX(o.baladia) as baladia,
            COUNT(o.id) as total_orders,
            COALESCE(SUM(o.total), 0) as total_spent,
            MAX(o.created_at) as last_order_date,
            MIN(o.created_at) as first_order_date
        FROM orders o
        WHERE o.status = 'DELIVERED'
        GROUP BY o.phone
        ORDER BY total_spent DESC
        """,
            countQuery = "SELECT COUNT(DISTINCT phone) FROM orders WHERE status = 'DELIVERED'",
            nativeQuery = true)
    List<Object[]> findAllCustomers(Pageable pageable);

    // Count total unique customers
    @Query(value = "SELECT COUNT(DISTINCT phone) FROM orders WHERE status = 'DELIVERED'", nativeQuery = true)
    Long countTotalCustomers();

    // Count active customers (with orders in last 30 days)
    @Query(value = """
        SELECT COUNT(DISTINCT phone) 
        FROM orders 
        WHERE status = 'DELIVERED' 
        AND created_at >= :date
        """, nativeQuery = true)
    Long countActiveCustomers(@Param("date") LocalDateTime date);

    // Count new customers this month
    @Query(value = """
        SELECT COUNT(DISTINCT phone) 
        FROM orders 
        WHERE status = 'DELIVERED' 
        AND created_at >= :startOfMonth
        AND phone NOT IN (
            SELECT DISTINCT phone 
            FROM orders 
            WHERE status = 'DELIVERED' 
            AND created_at < :startOfMonth
        )
        """, nativeQuery = true)
    Long countNewCustomers(@Param("startOfMonth") LocalDateTime startOfMonth);

    // Get customer details by phone - REMOVED email
    @Query(value = """
        SELECT 
            o.phone,
            MAX(CONCAT(o.first_name, ' ', o.last_name)) as name,
            MAX(o.wilaya) as wilaya,
            MAX(o.baladia) as baladia,
            COUNT(o.id) as total_orders,
            COALESCE(SUM(o.total), 0) as total_spent,
            MAX(o.created_at) as last_order_date,
            MIN(o.created_at) as first_order_date,
            AVG(o.total) as avg_order_value
        FROM orders o
        WHERE o.phone = :phone
        AND o.status = 'DELIVERED'
        GROUP BY o.phone
        """, nativeQuery = true)
    List<Object[]> findCustomerByPhone(@Param("phone") String phone);

    // Get customer orders
    @Query(value = """
        SELECT 
            o.id,
            o.total,
            o.status,
            o.created_at,
            (SELECT COUNT(*) FROM order_items WHERE order_id = o.id) as item_count
        FROM orders o
        WHERE o.phone = :phone
        ORDER BY o.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findOrdersByCustomerPhone(@Param("phone") String phone);

    // Get customer's top products
    @Query(value = """
        SELECT 
            g.id,
            g.name,
            b.name as brand,
            c.name as category,
            SUM(oi.quantity) as total_quantity,
            SUM(oi.price * oi.quantity) as total_spent
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        JOIN glasses g ON oi.glass_id = g.id
        LEFT JOIN brands b ON g.brand_id = b.id
        LEFT JOIN categories c ON g.category_id = c.id
        WHERE o.phone = :phone
        AND o.status = 'DELIVERED'
        GROUP BY g.id, g.name, b.name, c.name
        ORDER BY total_spent DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> findCustomerTopProducts(@Param("phone") String phone);

    // Get customers by wilaya
    @Query(value = """
        SELECT wilaya, COUNT(DISTINCT phone) as customer_count
        FROM orders
        WHERE status = 'DELIVERED'
        GROUP BY wilaya
        ORDER BY customer_count DESC
        """, nativeQuery = true)
    List<Object[]> countCustomersByWilaya();

    // Search customers - REMOVED email
    @Query(value = """
    SELECT DISTINCT 
        o.phone,
        MAX(CONCAT(o.first_name, ' ', o.last_name)) as name,
        MAX(o.wilaya) as wilaya,
        MAX(o.baladia) as baladia,
        COUNT(o.id) as total_orders,
        COALESCE(SUM(o.total), 0) as total_spent,
        MAX(o.created_at) as last_order_date
    FROM orders o
    WHERE o.status = 'DELIVERED'
    AND (
        LOWER(CONCAT(o.first_name, ' ', o.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))
        OR o.phone LIKE CONCAT('%', :search, '%')
    )
    GROUP BY o.phone
    ORDER BY total_spent DESC
    """, nativeQuery = true)
    List<Object[]> searchCustomers(@Param("search") String search, Pageable pageable);

    // Get repeat customers count
    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT phone, COUNT(*) as order_count
            FROM orders
            WHERE status = 'DELIVERED'
            GROUP BY phone
            HAVING COUNT(*) > 1
        ) as repeat_customers
        """, nativeQuery = true)
    Long countRepeatCustomers();

    // Get one-time customers count
    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT phone, COUNT(*) as order_count
            FROM orders
            WHERE status = 'DELIVERED'
            GROUP BY phone
            HAVING COUNT(*) = 1
        ) as one_time_customers
        """, nativeQuery = true)
    Long countOneTimeCustomers();

    // Get VIP customers (10+ orders or 100,000+ DZD spent)
    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT 
                phone,
                COUNT(*) as order_count,
                SUM(total) as total_spent
            FROM orders
            WHERE status = 'DELIVERED'
            GROUP BY phone
            HAVING COUNT(*) >= 10 OR SUM(total) >= 100000
        ) as vip_customers
        """, nativeQuery = true)
    Long countVIPCustomers();

    // Get customers by segment
    @Query(value = """
        SELECT 
            CASE 
                WHEN order_count >= 10 OR total_spent >= 100000 THEN 'VIP'
                WHEN order_count >= 3 THEN 'Regular'
                WHEN order_count >= 1 THEN 'New'
                ELSE 'Inactive'
            END as segment,
            COUNT(*) as count
        FROM (
            SELECT 
                phone,
                COUNT(*) as order_count,
                COALESCE(SUM(total), 0) as total_spent
            FROM orders
            WHERE status = 'DELIVERED'
            GROUP BY phone
        ) as customer_stats
        GROUP BY segment
        """, nativeQuery = true)
    List<Object[]> countCustomersBySegment();

    @Query(value = """
    SELECT COUNT(DISTINCT phone) 
    FROM orders 
    WHERE status = 'DELIVERED'
    AND (
        LOWER(CONCAT(first_name, ' ', last_name)) LIKE LOWER(CONCAT('%', :search, '%'))
        OR phone LIKE CONCAT('%', :search, '%')
    )
    """, nativeQuery = true)
    Long countSearchCustomers(@Param("search") String search);
}
