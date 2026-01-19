package com.opticstore.order.history.entity;

import com.opticstore.order.model.Order;
import com.opticstore.order.model.OrderStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String oldStatus;
    private String newStatus;
    private String changedBy;
    private LocalDateTime changedAt = LocalDateTime.now();



    public OrderStatusHistory() {}

    public OrderStatusHistory(
            Order order,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            String changedBy
    ) {
        this.order = order;
        this.oldStatus = oldStatus.name();
        this.newStatus = newStatus.name();
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }


    // getter & setter ...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
