package com.opticstore.order.history.service;

import com.opticstore.order.history.entity.OrderStatusHistory;
import com.opticstore.order.history.repository.OrderStatusHistoryRepository;
import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderStatusHistoryService {

    private final OrderStatusHistoryRepository repository;

    public OrderStatusHistoryService(OrderStatusHistoryRepository repository) {
        this.repository = repository;
    }

    public void logStatusChange(
            Order order,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            String changedBy
    ) {
        OrderStatusHistory history = new OrderStatusHistory(
                order,
                oldStatus,
                newStatus,
                changedBy
        );
        repository.save(history);
    }


    public List<OrderStatusHistory> getByOrder(Long orderId) {
        return repository.findByOrderIdOrderByChangedAtDesc(orderId);
    }

}

