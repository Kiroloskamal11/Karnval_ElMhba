package com.church.karneval.service;

import com.church.karneval.dto.RegisterRequest;
import com.church.karneval.dto.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    // ضيف
    AuthResponse login(String email, String password);
}
