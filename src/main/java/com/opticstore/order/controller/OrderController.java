package com.opticstore.order.controller;

import com.opticstore.order.dto.OrderRequest;
import com.opticstore.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

