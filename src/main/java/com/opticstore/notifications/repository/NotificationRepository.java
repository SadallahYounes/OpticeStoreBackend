package com.opticstore.notifications.repository;

import com.opticstore.notifications.model.Notification;
import com.opticstore.notifications.model.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();
    Long countByIsReadFalse();
    List<Notification> findByOrderId(Long orderId);
    List<Notification> findByType(NotificationType type);


    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findAllOrderByCreatedAtDesc(Pageable pageable);
}