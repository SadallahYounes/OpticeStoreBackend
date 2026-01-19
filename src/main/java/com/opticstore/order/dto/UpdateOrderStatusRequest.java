package com.opticstore.order.dto;

import com.opticstore.order.model.OrderStatus;

public record UpdateOrderStatusRequest(
        OrderStatus status
) {}