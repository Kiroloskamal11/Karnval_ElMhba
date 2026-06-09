package com.church.karneval.service.impl;

import com.church.karneval.enums.NotificationType;
import com.church.karneval.enums.UserRole;
import com.church.karneval.model.Notification;
import com.church.karneval.model.User;
import com.church.karneval.repository.NotificationRepository;
import com.church.karneval.repository.UserRepository;
import com.church.karneval.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Notification createNotification(UUID userId, NotificationType type, String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على المستخدم بالمعرف: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("لم يتم العثور على المستخدم بالمعرف: " + userId);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void createNotificationForAllAdmins(NotificationType type, String title, String content) {
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        List<User> superAdmins = userRepository.findByRole(UserRole.SUPER_ADMIN);

        admins.addAll(superAdmins);

        for (User admin : admins) {
            createNotification(admin.getId(), type, title, content);
        }
    }

    @Override
    @Transactional
    public void createNotificationForSuperAdminsOnly(NotificationType type, String title, String content) {
        List<User> superAdmins = userRepository.findByRole(UserRole.SUPER_ADMIN);

        for (User superAdmin : superAdmins) {
            createNotification(superAdmin.getId(), type, title, content);
        }
    }

    @Override
    @Transactional
    public Notification markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow();
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("غير مصرح لك بتعديل هذا الإشعار");
        }
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("لم يتم العثور على المستخدم بالمعرف: " + userId);
        }
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
}
