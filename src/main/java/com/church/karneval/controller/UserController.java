package com.church.karneval.controller;

import com.church.karneval.enums.UserStatus;
import com.church.karneval.model.User;
import com.church.karneval.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.church.karneval.enums.UserRole;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(required = false) UserStatus status,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        if (status != null) {
            return ResponseEntity.ok(userService.getUsersByStatus(status));
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        if (!currentUser.getId().equals(id) && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUserProfile(
            @PathVariable UUID id,
            @RequestBody User userDetails,
            @AuthenticationPrincipal User currentUser) {
        if (!currentUser.getId().equals(id) &&
                currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(
                userService.updateUserProfile(id, userDetails));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<User> rejectUser(
            @PathVariable UUID id,
            @RequestBody RejectRequest rejectRequest,
            @AuthenticationPrincipal User currentUser) {
        // ضيف
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(
                userService.rejectUser(id, currentUser.getId(),
                        rejectRequest.getReason()));
    }

    // الـ approve و reject المفروض Super Admin بس
    // ضيف فوق الـ method
    @PutMapping("/{id}/approve")
    // ✅ ضيف check
    public ResponseEntity<User> approveUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.approveUser(id, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    // ضيف @AuthenticationPrincipal وcheck
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    public static class RejectRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
