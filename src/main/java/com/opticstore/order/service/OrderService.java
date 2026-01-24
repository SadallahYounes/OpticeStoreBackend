package com.opticstore.order.service;

import com.opticstore.glasses.repository.GlassesRepository;
import com.opticstore.order.dto.*;
import com.opticstore.order.history.service.OrderStatusHistoryService;
import com.opticstore.order.mapper.AdminOrderMapper;
import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderItem;
import com.opticstore.order.model.OrderStatus;
import com.opticstore.order.model.PaymentMethod;
import com.opticstore.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryService statusHistoryService;

    public OrderService(
            OrderRepository orderRepository,
            OrderStatusHistoryService statusHistoryService
    ) {
        this.orderRepository = orderRepository;
        this.statusHistoryService = statusHistoryService;
    }

    public Order createOrder(OrderRequest request) {

        Order order = new Order();
        order.setFirstName(request.firstName());
        order.setLastName(request.lastName());
        order.setPhone(request.phone());
        order.setWilaya(request.wilaya());
        order.setBaladia(request.baladia());
        order.setAddress(request.address());
        order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        order.setStatus(OrderStatus.NEW);

        List<OrderItem> items = request.items().stream().map(i -> {
            OrderItem item = new OrderItem();
            item.setGlassId(i.glassId());
            item.setGlassName(i.glassName());
            item.setPrice(i.price());
            item.setQuantity(i.quantity());
            item.setOrder(order);
            return item;
        }).toList();

        order.setItems(items);

        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotal(total);

        return orderRepository.save(order);
    }


    //GET ADMIN ORDERS ...

    public Page<AdminOrderResponse> getAdminOrders(
            String search,
            OrderStatus status,
            String wilaya,
            Pageable pageable
    ) {
        Page<Order> orders = orderRepository.searchAdminOrders(
                search,
                status,
                wilaya,
                pageable
        );

        return orders.map(AdminOrderMapper::toResponse);
    }


    public OrderDetailsResponse getOrderDetails(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String status = order.getStatus() != null
                ? order.getStatus().name()
                : "NEW";

        return new OrderDetailsResponse(
                order.getId(),
                order.getFirstName(),
                order.getLastName(),
                order.getPhone(),
                order.getWilaya(),
                order.getBaladia(),
                order.getAddress(),
                order.getTotal(),
                status,
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(i -> new OrderItemResponse(
                                i.getGlassId(),
                                i.getGlassName(),
                                i.getPrice(),
                                i.getQuantity()
                        ))
                        .toList()
        );
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, String adminUsername) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus oldStatus = order.getStatus();

        if (oldStatus != newStatus) {
            order.setStatus(newStatus);
            orderRepository.save(order);

            statusHistoryService.logStatusChange(
                    order,
                    oldStatus,
                    newStatus,
                    adminUsername
            );
        }

        return order;
    }


}

