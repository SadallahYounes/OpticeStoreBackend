package com.opticstore.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerDTO {
    private Long id;
    private String phone;
    private String name;
    private String wilaya;
    private String baladia;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private LocalDateTime lastOrderDate;
    private LocalDateTime createdAt;

    public CustomerDTO(){}

    public CustomerDTO(Long id, String phone, String name, String wilaya, String baladia, Integer totalOrders, BigDecimal totalSpent, LocalDateTime lastOrderDate, LocalDateTime createdAt) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.wilaya = wilaya;
        this.baladia = baladia;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
        this.lastOrderDate = lastOrderDate;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWilaya() { return wilaya; }
    public void setWilaya(String wilaya) { this.wilaya = wilaya; }

    public String getBaladia() { return baladia; }
    public void setBaladia(String baladia) { this.baladia = baladia; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public LocalDateTime getLastOrderDate() { return lastOrderDate; }
    public void setLastOrderDate(LocalDateTime lastOrderDate) { this.lastOrderDate = lastOrderDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
