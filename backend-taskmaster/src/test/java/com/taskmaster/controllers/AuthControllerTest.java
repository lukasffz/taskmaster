package com.taskmaster.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmaster.dtos.auth.AuthResponse;
import com.taskmaster.dtos.auth.LoginRequest;
import com.taskmaster.dtos.auth.RegisterRequest;
import com.taskmaster.models.Role;
import com.taskmaster.security.CookieService;
import com.taskmaster.security.CustomUserDetailsService;
import com.taskmaster.security.JwtAuthenticationFilter;
import com.taskmaster.security.JwtService;
import com.taskmaster.services.AuthService;
import com.taskmaster.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private CookieService cookieService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldRegisterUserAndReturnCookie() throws Exception {
        RegisterRequest request = new RegisterRequest("Lukas User", "lukas@test.com", "password123");
        AuthResponse response = new AuthResponse(1L, "Lukas User", "lukas@test.com", Role.USER, "User registered successfully");
        AuthService.AuthResult result = new AuthService.AuthResult("mock.jwt.token", response);

        ResponseCookie cookie = ResponseCookie.from("taskmaster_token", "mock.jwt.token").httpOnly(true).build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(result);
        when(cookieService.createJwtCookie("mock.jwt.token")).thenReturn(cookie);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.email").value("lukas@test.com"))
                .andExpect(jsonPath("$.name").value("Lukas User"));
    }

    @Test
    void shouldLoginUserAndReturnCookie() throws Exception {
        LoginRequest request = new LoginRequest("lukas@test.com", "password123");
        AuthResponse response = new AuthResponse(1L, "Lukas User", "lukas@test.com", Role.USER, "Login successful");
        AuthService.AuthResult result = new AuthService.AuthResult("mock.jwt.token", response);

        ResponseCookie cookie = ResponseCookie.from("taskmaster_token", "mock.jwt.token").httpOnly(true).build();

        when(authService.login(any(LoginRequest.class))).thenReturn(result);
        when(cookieService.createJwtCookie("mock.jwt.token")).thenReturn(cookie);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.email").value("lukas@test.com"));
    }

    @Test
    void shouldLogoutAndClearCookie() throws Exception {
        ResponseCookie cleanCookie = ResponseCookie.from("taskmaster_token", "").maxAge(0).build();
        when(cookieService.createCleanJwtCookie()).thenReturn(cleanCookie);

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}
