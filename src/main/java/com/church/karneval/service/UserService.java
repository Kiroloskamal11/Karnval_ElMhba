package com.church.karneval.service;

import com.church.karneval.enums.UserRole;
import com.church.karneval.enums.UserStatus;
import com.church.karneval.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User approveUser(UUID userId, UUID approverId);

    User rejectUser(UUID userId, UUID approverId, String reason);

    List<User> getAllUsers();

    List<User> getUsersByStatus(UserStatus status);

    User getUserById(UUID id);

    User updateUserProfile(UUID id, User userDetails);

    void deleteUser(UUID id);

    // ضيف
    List<User> getUsersByRole(UserRole role);
}
