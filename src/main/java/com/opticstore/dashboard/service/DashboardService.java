package com.opticstore.dashboard.service;

import com.opticstore.dashboard.dto.DashboardStatsResponse;
import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderStatus;
import com.opticstore.order.repository.OrderRepository;


import com.opticstore.security.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public DashboardService(
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public DashboardStatsResponse getDashboardStats() {
        LocalDate today = LocalDate.now();

        // Basic stats
        long totalOrders = orderRepository.totalOrders();

        // FIX: Calculate total revenue from ONLY DELIVERED orders
        BigDecimal totalRevenueBD = orderRepository.findByStatus(OrderStatus.DELIVERED).stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double totalRevenue = totalRevenueBD.doubleValue();

        // Status counts
        long pendingOrders = orderRepository.countByStatus(OrderStatus.NEW);
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long shippedOrders = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long canceledOrders = orderRepository.countByStatus(OrderStatus.CANCELED);

        // Today's orders
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();
        long todayOrders = orderRepository.countByCreatedAtBetween(startOfToday, endOfToday);

        // Active customers = unique customers who placed DELIVERED orders
        long activeCustomers = orderRepository.countUniqueCustomersWithDeliveredOrders();

        // FIX: Average order value should use DELIVERED orders only
        double averageOrderValue = deliveredOrders > 0 ?
                totalRevenueBD.divide(BigDecimal.valueOf(deliveredOrders), 2, RoundingMode.HALF_UP).doubleValue() : 0.0;

        // Simple growth calculations (set to 0 for now)
        double orderGrowth = 0.0;
        double revenueGrowth = 0.0;
        double pendingChange = 0.0;
        double deliveredChange = 0.0;

        // Period revenues - FIX: Use methods that filter by DELIVERED status
        // Daily revenue - only DELIVERED orders today
        BigDecimal dailyRevenueBD = orderRepository.sumRevenueByStatusBetweenDates(
                OrderStatus.DELIVERED, startOfToday, endOfToday);
        double dailyRevenue = dailyRevenueBD != null ? dailyRevenueBD.doubleValue() : 0.0;

        // Weekly revenue (last 7 days) - only DELIVERED orders
        LocalDateTime startOfWeek = today.minusDays(7).atStartOfDay();
        BigDecimal weeklyRevenueBD = orderRepository.sumRevenueByStatusBetweenDates(
                OrderStatus.DELIVERED, startOfWeek, endOfToday);
        double weeklyRevenue = weeklyRevenueBD != null ? weeklyRevenueBD.doubleValue() : 0.0;

        // Monthly revenue (last 30 days) - only DELIVERED orders
        LocalDateTime startOfMonth = today.minusDays(30).atStartOfDay();
        BigDecimal monthlyRevenueBD = orderRepository.sumRevenueByStatusBetweenDates(
                OrderStatus.DELIVERED, startOfMonth, endOfToday);
        double monthlyRevenue = monthlyRevenueBD != null ? monthlyRevenueBD.doubleValue() : 0.0;

        return new DashboardStatsResponse(
                totalOrders,
                totalRevenue,
                pendingOrders,           // NEW orders
                deliveredOrders,
                todayOrders,
                activeCustomers,         // Now only customers with DELIVERED orders
                averageOrderValue,       // Now based on DELIVERED orders only
                orderGrowth,
                revenueGrowth,
                pendingChange,
                deliveredChange,
                dailyRevenue,            // Now only DELIVERED orders today
                weeklyRevenue,           // Now only DELIVERED orders in last 7 days
                monthlyRevenue,          // Now only DELIVERED orders in last 30 days
                confirmedOrders,
                shippedOrders,
                canceledOrders
        );
    }

    private BigDecimal calculateTotalRevenue(List<Order> orders) {
        return orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRevenueBetween(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        return orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRevenueAfter(LocalDateTime date) {
        List<Order> orders = orderRepository.findByCreatedAtAfter(date);
        return orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculateOrderGrowth(LocalDate today) {
        // Compare with same day last week
        LocalDate lastWeek = today.minusDays(7);
        LocalDateTime startOfLastWeek = lastWeek.atStartOfDay();
        LocalDateTime endOfLastWeek = lastWeek.plusDays(1).atStartOfDay();

        Long ordersLastWeek = orderRepository.countByCreatedAtBetween(startOfLastWeek, endOfLastWeek);
        Long ordersToday = orderRepository.countByCreatedAtBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        if (ordersLastWeek == null || ordersToday == null || ordersLastWeek == 0) return 0.0;
        return ((double) (ordersToday - ordersLastWeek) / ordersLastWeek) * 100;
    }

    private double calculateRevenueGrowth(LocalDate today) {
        // Compare revenue with same day last week
        LocalDate lastWeek = today.minusDays(7);
        LocalDateTime startOfLastWeek = lastWeek.atStartOfDay();
        LocalDateTime endOfLastWeek = lastWeek.plusDays(1).atStartOfDay();

        BigDecimal revenueLastWeekBD = calculateRevenueBetween(startOfLastWeek, endOfLastWeek);
        BigDecimal revenueTodayBD = calculateRevenueBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        double revenueLastWeek = revenueLastWeekBD.doubleValue();
        double revenueToday = revenueTodayBD.doubleValue();

        if (revenueLastWeek == 0.0) return 0.0;
        return ((revenueToday - revenueLastWeek) / revenueLastWeek) * 100;
    }
}