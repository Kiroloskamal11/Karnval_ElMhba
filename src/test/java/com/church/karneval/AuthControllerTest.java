package com.church.karneval;

import com.church.karneval.dto.AuthResponse;
import com.church.karneval.dto.RegisterRequest;
import com.church.karneval.service.AuthService;
import com.church.karneval.enums.UserRole;
import com.church.karneval.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AuthService authService;

        @BeforeEach
        void setup() {
        }

        @Test
        void registerAdmin_success() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setEmail("admin@example.com");
                req.setPassword("Password123");
                req.setRole(UserRole.ADMIN);

                AuthResponse resp = new AuthResponse(UUID.randomUUID(), "Admin", "admin@example.com", UserRole.ADMIN,
                                UserStatus.APPROVED, "dummy-token");
                Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Admin\",\"email\":\"admin@example.com\",\"password\":\"Password123\",\"role\":\"ADMIN\"}"))
                                .andExpect(status().isCreated());
        }

        @Test
        void registerTeamLeader_withTeamId_success() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setEmail("leader@example.com");
                req.setPassword("Password123");
                req.setRole(UserRole.TEAM_LEADER);
                req.setTeamId(UUID.randomUUID());

                AuthResponse resp = new AuthResponse(UUID.randomUUID(), "Leader", "leader@example.com",
                                UserRole.TEAM_LEADER, UserStatus.APPROVED, "dummy-token");
                Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Leader\",\"email\":\"leader@example.com\",\"password\":\"Password123\",\"role\":\"TEAM_LEADER\",\"teamId\":\"00000000-0000-0000-0000-000000000001\"}"))
                                .andExpect(status().isCreated());
        }

        @Test
        void registerTeamLeader_withoutTeamId_badRequest() throws Exception {
                Mockito.when(authService.register(any(RegisterRequest.class)))
                                .thenThrow(new RuntimeException("قائد الفريق يجب أن يختار لون الفريق."));
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Leader\",\"email\":\"leader@example.com\",\"password\":\"Password123\",\"role\":\"TEAM_LEADER\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void registerDuplicateEmail_badRequest() throws Exception {
                Mockito.when(authService.register(any(RegisterRequest.class)))
                                .thenThrow(new RuntimeException("Email already exists"));
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Admin\",\"email\":\"dup@example.com\",\"password\":\"Password123\",\"role\":\"ADMIN\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void login_success() throws Exception {
                AuthResponse resp = new AuthResponse(UUID.randomUUID(), "User", "user@example.com",
                                UserRole.TEAM_LEADER, UserStatus.APPROVED, "jwt-token");
                Mockito.when(authService.login(Mockito.anyString(), Mockito.anyString())).thenReturn(resp);
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"user@example.com\",\"password\":\"Password123\"}"))
                                .andExpect(status().isOk());
        }

        @Test
        void loginPending_badRequest() throws Exception {
                Mockito.when(authService.login(Mockito.anyString(), Mockito.anyString()))
                                .thenThrow(new RuntimeException("حسابك قيد المراجعة. انتظر موافقة المسؤول."));
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"pending@example.com\",\"password\":\"Password123\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void loginRejected_badRequest() throws Exception {
                Mockito.when(authService.login(Mockito.anyString(), Mockito.anyString()))
                                .thenThrow(new RuntimeException("تم رفض طلب تسجيلك."));
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"rejected@example.com\",\"password\":\"Password123\"}"))
                                .andExpect(status().isBadRequest());
        }
}
