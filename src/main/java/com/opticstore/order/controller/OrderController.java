package com.opticstore.order.controller;

import com.opticstore.order.dto.OrderItemRequest;
import com.opticstore.order.dto.OrderRequest;
import com.opticstore.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PostMapping("/validate-stock")
    public ResponseEntity<?> validateStock(@RequestBody List<OrderItemRequest> items) {
        try {
            Map<String, Object> validation = orderService.validateStockBeforeOrder(items);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Stock validation failed: " + e.getMessage()));
        }
    }
}

