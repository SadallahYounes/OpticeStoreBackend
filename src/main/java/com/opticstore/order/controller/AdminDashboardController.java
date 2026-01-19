package com.opticstore.order.controller;

import com.opticstore.order.dto.DashboardStatsResponse;
import com.opticstore.order.dto.SalesByDayResponse;
import com.opticstore.order.service.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService service;

    public AdminDashboardController(AdminDashboardService service) {
        this.service = service;
    }

    @GetMapping("/stats")
    public DashboardStatsResponse stats() {
        return service.getStats();
    }

    @GetMapping("/sales-by-day")
    public List<SalesByDayResponse> salesByDay() {
        return service.getSalesByDay();
    }
}