package com.opticstore.order.controller;

import com.opticstore.order.dto.AdminOrderResponse;
import com.opticstore.order.dto.OrderDetailsResponse;
import com.opticstore.order.dto.UpdateOrderStatusRequest;
import com.opticstore.order.mapper.AdminOrderMapper;
import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderStatus;
import com.opticstore.order.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<AdminOrderResponse> getOrders(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String wilaya,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return orderService.getAdminOrders(search, status, wilaya, pageable);
    }

   /* @GetMapping
    public Page<AdminOrderResponse> getOrders(
            @RequestParam(defaultValue = "") String search,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return orderService.getAdminOrders(search, pageable);
    }*/

    @GetMapping("/{id}")
    public OrderDetailsResponse getOrder(@PathVariable Long id) {
        return orderService.getOrderDetails(id);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AdminOrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request,
            Principal principal
    ) {
        Order order = orderService.updateOrderStatus(
                id,
                request.status(),           // enum, not string
                principal.getName()          //  admin username
        );

        return ResponseEntity.ok(AdminOrderMapper.toResponse(order));
    }

}
