package com.church.karneval;

import com.church.karneval.model.User;
import com.church.karneval.enums.UserRole;
import com.church.karneval.enums.UserStatus;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class TestUtil {
    public static User mockUser(UUID id, UserRole role, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    public static void setAuthentication(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                java.util.Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public static org.springframework.test.web.servlet.request.RequestPostProcessor securityContextOf(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext(context);
    }
}
