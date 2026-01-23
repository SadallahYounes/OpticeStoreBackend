package com.opticstore.order.history.controller;

import com.opticstore.order.history.dto.OrderStatusHistoryResponse;
import com.opticstore.order.history.entity.OrderStatusHistory;
import com.opticstore.order.history.service.OrderStatusHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class OrderStatusHistoryController {

    private final OrderStatusHistoryService service;

    public OrderStatusHistoryController(OrderStatusHistoryService service) {
        this.service = service;
    }

    @GetMapping("/{orderId}/status-history")
    public List<OrderStatusHistoryResponse> getHistory(@PathVariable Long orderId) {
        return service.getByOrder(orderId);
    }
}