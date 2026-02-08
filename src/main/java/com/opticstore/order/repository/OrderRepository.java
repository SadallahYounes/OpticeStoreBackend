package com.opticstore.order.repository;

import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByStatus(OrderStatus status);

    //  Return BigDecimal instead of Double
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    BigDecimal sumRevenueBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    //Return BigDecimal instead of Double
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

    //  count by unique phone + name combination
    @Query(value = "SELECT COUNT(DISTINCT CONCAT(first_name, ' ', last_name, ' ', phone)) FROM orders", nativeQuery = true)
    Long countUniqueCustomerIdentities();
}

