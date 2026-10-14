package com.nit.notification;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(
            Long userId,
            String type,
            String message) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Notification type is required.");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Notification message is required.");
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type.trim());
        notification.setMessage(message.trim());
        notification.setIsRead(0);

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, 0);
    }

    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found."));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException(
                    "You are not allowed to update this notification.");
        }

        notification.setIsRead(1);

        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(userId);

        for (Notification notification : notifications) {
            notification.setIsRead(1);
        }

        notificationRepository.saveAll(notifications);
    }
}