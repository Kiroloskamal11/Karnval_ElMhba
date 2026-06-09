package com.church.karneval.service.impl;

import com.church.karneval.enums.NotificationType;
import com.church.karneval.enums.UserRole;
import com.church.karneval.enums.UserStatus;
import com.church.karneval.model.Notification;
import com.church.karneval.model.User;
import com.church.karneval.repository.NotificationRepository;
import com.church.karneval.repository.UserRepository;
import com.church.karneval.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public UserServiceImpl(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public User approveUser(UUID userId, UUID approverId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود."));
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("المشرف المعتمد غير موجود."));

        // Validate duplicates
        if (user.getRole() == UserRole.TEAM_LEADER && user.getTeam() != null) {
            boolean hasApprovedLeader = userRepository.existsByTeamIdAndStatus(user.getTeam().getId(), UserStatus.APPROVED);
            if (hasApprovedLeader) {
                throw new RuntimeException("هناك قائد فريق معتمد بالفعل لهذا اللون.");
            }
        }

        if (user.getRole() == UserRole.CAMP_LEADER && user.getStation() != null) {
            boolean hasApprovedLeader = userRepository.existsByStationIdAndStatus(user.getStation().getId(), UserStatus.APPROVED);
            if (hasApprovedLeader) {
                throw new RuntimeException("هناك مسؤول محطة معتمد بالفعل لهذه المحطة.");
            }
        }

        user.setStatus(UserStatus.APPROVED);
        user.setApprovedBy(approver);
        user.setApprovedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        User savedUser = userRepository.save(user);

        // Send Notification
        Notification notification = new Notification();
        notification.setUser(savedUser);
        notification.setType(NotificationType.REGISTRATION_APPROVED);
        notification.setTitle("تم تفعيل حسابك");
        notification.setContent("تهانينا! لقد تمت الموافقة على حسابك كـ " + getRoleArabicLabel(savedUser.getRole()));
        notification.setCreatedAt(OffsetDateTime.now());
        notificationRepository.save(notification);

        return savedUser;
    }

    @Override
    @Transactional
    public User rejectUser(UUID userId, UUID approverId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود."));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("المشرف المعتمد غير موجود."));

        user.setStatus(UserStatus.REJECTED);
        user.setApprovedBy(approver);
        user.setApprovedAt(OffsetDateTime.now());
        user.setRejectedReason(reason);
        user.setTeam(null);
        user.setStation(null);
        user.setUpdatedAt(OffsetDateTime.now());

        User savedUser = userRepository.save(user);

        // Send Notification
        Notification notification = new Notification();
        notification.setUser(savedUser);
        notification.setType(NotificationType.REGISTRATION_REJECTED);
        notification.setTitle("تم رفض تفعيل حسابك");
        notification.setContent("تم رفض الحساب للأسباب التالية: " + reason);
        notification.setCreatedAt(OffsetDateTime.now());
        notificationRepository.save(notification);

        return savedUser;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود."));
    }

    @Override
    @Transactional
    public User updateUserProfile(UUID id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود."));

        user.setName(userDetails.getName());
        user.setRole(userDetails.getRole());
        user.setTeam(userDetails.getTeam());
        user.setStation(userDetails.getStation());
        user.setUpdatedAt(OffsetDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود."));
        userRepository.delete(user);
    }

    @Override
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    private String getRoleArabicLabel(UserRole role) {

        switch (role) {
            case SUPER_ADMIN: return "مسؤول عام للكرنفال";
            case ADMIN: return "متابع كنسي";
            case TEAM_LEADER: return "قائد مجموعة";
            case CAMP_LEADER: return "مسؤول محطة";
            default: return role.name();
        }
    }
}
