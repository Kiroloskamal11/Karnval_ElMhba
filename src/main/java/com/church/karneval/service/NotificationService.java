package com.church.karneval.service;

import com.church.karneval.enums.NotificationType;
import com.church.karneval.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    Notification createNotification(UUID userId, NotificationType type, String title, String content);

    List<Notification> getUserNotifications(UUID userId);

    Notification markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    void createNotificationForAllAdmins(NotificationType type, String title, String content);

    void createNotificationForSuperAdminsOnly(NotificationType type, String title, String content);
}

