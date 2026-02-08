package com.opticstore.notifications.controller;

import com.opticstore.notifications.dto.NotificationResponse;
import com.opticstore.notifications.model.Notification;
import com.opticstore.notifications.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @RequestParam(defaultValue = "50") int limit) {

        // Get both read and unread, but limit the number
        return notificationService.getAllNotifications(limit).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/unread")
    public List<NotificationResponse> getUnreadNotifications() {
        return notificationService.getUnreadNotifications().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/count")
    public Map<String, Long> getUnreadCount() {
        return Map.of("count", notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id) {
        return toResponse(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType().toString(),
                notification.getPriority().toString(),
                notification.getOrder() != null ? notification.getOrder().getId() : null,
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}