package com.opticstore.order.dto;

import java.util.List;

public record OrderRequest(
        String firstName,
        String lastName,
        String phone,
        String wilaya,
        String baladia,
        String address,
        List<OrderItemRequest> items
) {}