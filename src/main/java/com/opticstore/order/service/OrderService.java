package com.opticstore.order.service;

import com.opticstore.glasses.repository.GlassesRepository;
import com.opticstore.order.dto.OrderItemRequest;
import com.opticstore.order.dto.OrderRequest;
import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderItem;
import com.opticstore.order.model.OrderStatus;
import com.opticstore.order.model.PaymentMethod;
import com.opticstore.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(OrderRequest request) {

        Order order = new Order();
        order.setFirstName(request.firstName());
        order.setLastName(request.lastName());
        order.setPhone(request.phone());
        order.setWilaya(request.wilaya());
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
}

