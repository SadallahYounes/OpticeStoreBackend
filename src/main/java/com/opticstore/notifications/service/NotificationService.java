package com.opticstore.notifications.service;

import com.opticstore.notifications.model.Notification;
import com.opticstore.notifications.model.NotificationPriority;
import com.opticstore.notifications.model.NotificationType;
import com.opticstore.notifications.repository.NotificationRepository;
import com.opticstore.order.model.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    public NotificationService(
            NotificationRepository notificationRepository,
            SseEmitterService sseEmitterService) {
        this.notificationRepository = notificationRepository;
        this.sseEmitterService = sseEmitterService;
    }

    public Notification createNotification(
            String title,
            String message,
            NotificationType type,
            NotificationPriority priority,
            Order order) {

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setOrder(order);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);

        // Send SSE event to all connected admins
        sendSSENotification(saved);

        return saved;
    }

    private void sendSSENotification(Notification notification) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", notification.getId());
        eventData.put("title", notification.getTitle());
        eventData.put("message", notification.getMessage());
        eventData.put("type", notification.getType().toString()); // Use toString() instead of .name()
        eventData.put("priority", notification.getPriority().toString()); // Use toString() instead of .name()
        eventData.put("orderId", notification.getOrder() != null ? notification.getOrder().getId() : null);
        eventData.put("createdAt", notification.getCreatedAt());
        eventData.put("isRead", notification.isRead());

        // Send to all connected clients
        sseEmitterService.sendToAll("new-notification", eventData);
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    // ADD THIS METHOD
    public List<Notification> getAllNotifications(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findAllOrderByCreatedAtDesc(pageable);
    }

    public Long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // ADD THIS METHOD
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}