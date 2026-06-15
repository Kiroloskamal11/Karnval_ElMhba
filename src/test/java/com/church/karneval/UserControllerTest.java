package com.church.karneval;

import com.church.karneval.enums.UserRole;
import com.church.karneval.enums.UserStatus;
import com.church.karneval.model.User;
import com.church.karneval.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User superAdmin;
    private User admin;
    private User otherUser;

    @BeforeEach
    void setup() {
        UUID superId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        superAdmin = TestUtil.mockUser(superId, UserRole.SUPER_ADMIN, UserStatus.APPROVED);
        admin = TestUtil.mockUser(adminId, UserRole.ADMIN, UserStatus.APPROVED);
        otherUser = TestUtil.mockUser(otherId, UserRole.TEAM_LEADER, UserStatus.APPROVED);
    }

    @Test
    void getAllUsers_superAdmin_ok() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/users").with(TestUtil.securityContextOf(superAdmin)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_admin_forbidden() throws Exception {
        mockMvc.perform(get("/users").with(TestUtil.securityContextOf(admin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_self_ok() throws Exception {
        when(userService.getUserById(otherUser.getId())).thenReturn(otherUser);
        mockMvc.perform(get("/users/" + otherUser.getId()).with(TestUtil.securityContextOf(otherUser)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_other_superAdmin_ok() throws Exception {
        when(userService.getUserById(otherUser.getId())).thenReturn(otherUser);
        mockMvc.perform(get("/users/" + otherUser.getId()).with(TestUtil.securityContextOf(superAdmin)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_other_notSuperAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/users/" + otherUser.getId()).with(TestUtil.securityContextOf(admin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void approveUser_superAdmin_ok() throws Exception {
        when(userService.approveUser(any(UUID.class), any(UUID.class))).thenReturn(otherUser);
        mockMvc.perform(put("/users/" + otherUser.getId() + "/approve").with(TestUtil.securityContextOf(superAdmin)))
                .andExpect(status().isOk());
    }

    @Test
    void approveUser_admin_forbidden() throws Exception {
        mockMvc.perform(put("/users/" + otherUser.getId() + "/approve").with(TestUtil.securityContextOf(admin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectUser_superAdmin_ok() throws Exception {
        when(userService.rejectUser(any(UUID.class), any(UUID.class), any(String.class))).thenReturn(otherUser);
        String json = "{\"reason\":\"Not suitable\"}";
        mockMvc.perform(
                put("/users/" + otherUser.getId() + "/reject").contentType(MediaType.APPLICATION_JSON).content(json)
                        .with(TestUtil.securityContextOf(superAdmin)))
                .andExpect(status().isOk());
    }

    @Test
    void rejectUser_admin_forbidden() throws Exception {
        String json = "{\"reason\":\"Not suitable\"}";
        mockMvc.perform(
                put("/users/" + otherUser.getId() + "/reject").contentType(MediaType.APPLICATION_JSON).content(json)
                        .with(TestUtil.securityContextOf(admin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_superAdmin_noContent() throws Exception {
        mockMvc.perform(delete("/users/" + otherUser.getId()).with(TestUtil.securityContextOf(superAdmin)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_admin_forbidden() throws Exception {
        mockMvc.perform(delete("/users/" + otherUser.getId()).with(TestUtil.securityContextOf(admin)))
                .andExpect(status().isForbidden());
    }
}
