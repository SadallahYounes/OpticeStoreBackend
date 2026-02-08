package com.opticstore.notifications.dto;

import com.opticstore.notifications.model.NotificationPriority;
import com.opticstore.notifications.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String type,
        String priority,
        Long orderId,
        LocalDateTime createdAt,
        boolean isRead
) {}