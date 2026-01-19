package com.opticstore.order.service;

import com.opticstore.order.dto.DashboardStatsResponse;
import com.opticstore.order.dto.SalesByDayResponse;
import com.opticstore.order.model.OrderStatus;
import com.opticstore.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminDashboardService {

    private final OrderRepository repo;

    public AdminDashboardService(OrderRepository repo) {
        this.repo = repo;
    }

    public DashboardStatsResponse getStats() {
        return new DashboardStatsResponse(
                repo.totalOrders(),
                repo.totalRevenue(),
                repo.countByStatus(OrderStatus.NEW),
                repo.countByStatus(OrderStatus.DELIVERED)
        );
    }

    public List<SalesByDayResponse> getSalesByDay() {
        return repo.salesByDay().stream()
                .map(r -> new SalesByDayResponse(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        (BigDecimal) r[1]
                ))
                .toList();
    }
}

