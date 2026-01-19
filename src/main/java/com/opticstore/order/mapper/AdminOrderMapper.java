package com.opticstore.order.mapper;

import com.opticstore.order.dto.AdminOrderResponse;
import com.opticstore.order.model.Order;

public class AdminOrderMapper {

    public static AdminOrderResponse toResponse(Order order) {
        return new AdminOrderResponse(
                order.getId(),
                order.getFirstName() + " " + order.getLastName(),
                order.getPhone(),
                order.getWilaya(),
                order.getBaladia(),
                order.getTotal(),
                order.getStatus().name(), //  enum → string here only
                order.getCreatedAt()
        );
    }
}
