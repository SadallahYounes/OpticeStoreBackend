package com.opticstore.order.service;

import com.opticstore.notifications.model.NotificationPriority;
import com.opticstore.notifications.model.NotificationType;
import com.opticstore.notifications.service.NotificationService;
import com.opticstore.order.dto.*;
import com.opticstore.order.history.service.OrderStatusHistoryService;
import com.opticstore.order.mapper.AdminOrderMapper;
import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderItem;
import com.opticstore.order.model.OrderStatus;
import com.opticstore.order.model.PaymentMethod;
import com.opticstore.order.repository.OrderRepository;
import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.product.glasses.repository.GlassesRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryService statusHistoryService;
    private final NotificationService notificationService;
    private final GlassesRepository glassesRepository;


    public OrderService(
            OrderRepository orderRepository,
            OrderStatusHistoryService statusHistoryService,
            NotificationService notificationService,
            GlassesRepository glassesRepository
    ) {
        this.orderRepository = orderRepository;
        this.statusHistoryService = statusHistoryService;
        this.notificationService = notificationService;
        this.glassesRepository = glassesRepository;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {

        // First, validate and reserve stock for all items
        for (OrderItemRequest item : request.items()) {
            Glasses glasses = glassesRepository.findById(item.glassId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + item.glassId()));

            // Check if enough stock is available using isInStock() helper
            if (!glasses.isInStock() || glasses.getQuantity() < item.quantity()) {
                throw new RuntimeException(
                        String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                                glasses.getName(), glasses.getQuantity(), item.quantity())
                );
            }
        }

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

        // Save the order first
        Order savedOrder = orderRepository.save(order);

        // Now update stock for each item using decreaseQuantity method
        for (OrderItemRequest item : request.items()) {
            Glasses glasses = glassesRepository.findById(item.glassId()).get();

            // Use the existing decreaseQuantity method
            glasses.decreaseQuantity(item.quantity());

            glassesRepository.save(glasses);

            // Create low stock notification if threshold is reached
            if (glasses.getQuantity() <= 5) {
                notificationService.createSystemNotification(
                    "Low Stock Alert",
                    String.format("Product '%s' is running low. Current stock: %d",
                        glasses.getName(), glasses.getQuantity()),
                    NotificationType.LOW_STOCK,
                    NotificationPriority.HIGH
                );
            }
        }

        // Notification: new order created
        notificationService.createNotification(
                "New Order Received",
                String.format(
                        "Order #%d from %s %s",
                        savedOrder.getId(),
                        savedOrder.getFirstName(),
                        savedOrder.getLastName()
                ),
                NotificationType.ORDER_CREATED,
                NotificationPriority.HIGH,
                savedOrder
        );

        return savedOrder;
    }


    // restore stock when an order is canceled
    @Transactional
    public void restoreStockForCanceledOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            Glasses glasses = glassesRepository.findById(item.getGlassId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getGlassId()));

            glasses.increaseQuantity(item.getQuantity());
            glassesRepository.save(glasses);
        }
    }


    //check stock before placing order
    public Map<String, Object> validateStockBeforeOrder(List<OrderItemRequest> items) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> unavailableItems = new ArrayList<>();
        boolean allAvailable = true;

        for (OrderItemRequest item : items) {
            Glasses glasses = glassesRepository.findById(item.glassId()).orElse(null);
            if (glasses == null) {
                Map<String, Object> unavailable = new HashMap<>();
                unavailable.put("glassId", item.glassId());
                unavailable.put("reason", "Product not found");
                unavailableItems.add(unavailable);
                allAvailable = false;
            } else if (!glasses.isInStock() || glasses.getQuantity() < item.quantity()) {
                Map<String, Object> unavailable = new HashMap<>();
                unavailable.put("glassId", item.glassId());
                unavailable.put("name", glasses.getName());
                unavailable.put("available", glasses.getQuantity());
                unavailable.put("requested", item.quantity());
                unavailable.put("reason", "Insufficient stock");
                unavailableItems.add(unavailable);
                allAvailable = false;
            }
        }

        result.put("allAvailable", allAvailable);
        result.put("unavailableItems", unavailableItems);
        return result;
    }


    // GET ADMIN ORDERS
    public Page<AdminOrderResponse> getAdminOrders(
            String search,
            OrderStatus status,
            String wilaya,
            Pageable pageable
    ) {
        return orderRepository.searchAdminOrders(
                search == null ? "" : search,
                status,
                wilaya,
                pageable
        ).map(AdminOrderMapper::toResponse);
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
            // If order is being canceled, restore stock
            if (newStatus == OrderStatus.CANCELED && oldStatus != OrderStatus.CANCELED) {
                restoreStockForCanceledOrder(order);
            }

            // If order was canceled and is being reactivated, reduce stock again
            if (oldStatus == OrderStatus.CANCELED && newStatus != OrderStatus.CANCELED) {
                // Check stock availability before reactivating
                for (OrderItem item : order.getItems()) {
                    Glasses glasses = glassesRepository.findById(item.getGlassId()).get();
                    if (glasses.getQuantity() < item.getQuantity()) {
                        throw new RuntimeException(
                                "Cannot reactivate order: insufficient stock for " + glasses.getName()
                        );
                    }
                }
                // Reduce stock again using decreaseQuantity
                for (OrderItem item : order.getItems()) {
                    Glasses glasses = glassesRepository.findById(item.getGlassId()).get();
                    glasses.decreaseQuantity(item.getQuantity());
                    glassesRepository.save(glasses);
                }
            }

            order.setStatus(newStatus);
            orderRepository.save(order);

            statusHistoryService.logStatusChange(
                    order,
                    oldStatus,
                    newStatus,
                    adminUsername
            );

            notificationService.createNotification(
                    "Order Status Updated",
                    String.format(
                            "Order #%d changed from %s to %s",
                            order.getId(),
                            oldStatus,
                            newStatus
                    ),
                    NotificationType.ORDER_STATUS_CHANGED,
                    NotificationPriority.MEDIUM,
                    order
            );
        }

        return order;
    }
}

