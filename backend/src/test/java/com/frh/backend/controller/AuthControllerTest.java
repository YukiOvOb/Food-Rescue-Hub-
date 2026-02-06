package com.frh.backend.controller;

import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.AuthResponse;
import com.frh.backend.dto.LoginRequest;
import com.frh.backend.dto.RegisterRequest;
import com.frh.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    /* --------------------------------
       REGISTER – CONSUMER
       -------------------------------- */
    @Test
    void register_consumer_success() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("consumer@test.com");
        request.setPassword("Password@123");
        request.setDisplayName("Consumer User");
        request.setRole("CONSUMER");

        AuthResponse response = new AuthResponse("token", 0L, "consumer@test.com", null, "CONSUMER", "Consumer registered");

        var consumer = Mockito.mock(ConsumerProfile.class);

        Mockito.when(authService.register(Mockito.any()))
                .thenReturn(response);

        Mockito.when(authService.getConsumerByEmail(request.getEmail()))
                .thenReturn(consumer);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       REGISTER – SUPPLIER
       -------------------------------- */
    @Test
    void register_supplier_success() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("supplier@test.com");
        request.setPassword("Password@123");
        request.setDisplayName("Supplier User");
        request.setRole("SUPPLIER");

        AuthResponse response = new AuthResponse("token", 10L, "supplier@test.com", null, "SUPPLIER", "Supplier registered");

        SupplierProfile supplier = new SupplierProfile();
        supplier.setSupplierId(10L);

        Mockito.when(authService.register(Mockito.any()))
                .thenReturn(response);

        Mockito.when(authService.getSupplierByEmail(request.getEmail()))
                .thenReturn(supplier);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       LOGIN
       -------------------------------- */
    @Test
    void login_success() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("Password@123");

        AuthResponse response = new AuthResponse("token", 1L, "user@test.com", null, "CONSUMER", "Login successful");

        Mockito.when(authService.login(Mockito.any(), Mockito.any(HttpSession.class)))
                .thenAnswer(invocation -> {
                    HttpSession session = invocation.getArgument(1);
                    session.setAttribute("USER_ID", 1L);
                    session.setAttribute("USER_ROLE", "CONSUMER");
                    return response;
                });

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       GET CURRENT USER – NOT LOGGED IN
       -------------------------------- */
    @Test
    void getCurrentUser_unauthorized() throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    /* --------------------------------
       GET CURRENT USER – LOGGED IN
       -------------------------------- */
    @Test
    void getCurrentUser_success() throws Exception {

        mockMvc.perform(get("/api/auth/me")
                        .sessionAttr("USER_ID", 1L)
                        .sessionAttr("USER_ROLE", "CONSUMER")
                        .sessionAttr("USER_EMAIL", "user@test.com")
                        .sessionAttr("USER_DISPLAY_NAME", "Test User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").value("CONSUMER"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.displayName").value("Test User"));
    }

    /* --------------------------------
       LOGOUT
       -------------------------------- */
    @Test
    void logout_success() throws Exception {

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    /* --------------------------------
       VALIDATION ERROR
       -------------------------------- */
    @Test
    void validation_error() throws Exception {

        RegisterRequest invalidRequest = new RegisterRequest();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
