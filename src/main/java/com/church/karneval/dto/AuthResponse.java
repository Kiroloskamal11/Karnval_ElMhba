package com.church.karneval.dto;

import com.church.karneval.enums.UserRole;
import com.church.karneval.enums.UserStatus;
import java.util.UUID;

public class AuthResponse {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private String message;

    public AuthResponse(UUID id, String name, String email, UserRole role, UserStatus status, String message) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
