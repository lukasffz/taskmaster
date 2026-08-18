package com.taskmaster.controllers;

import com.taskmaster.dtos.auth.AuthResponse;
import com.taskmaster.dtos.auth.LoginRequest;
import com.taskmaster.dtos.auth.RegisterRequest;
import com.taskmaster.dtos.user.UserResponse;
import com.taskmaster.models.User;
import com.taskmaster.security.CookieService;
import com.taskmaster.services.AuthService;
import com.taskmaster.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final CookieService cookieService;

    public AuthController(
            AuthService authService,
            UserService userService,
            CookieService cookieService
    ) {
        this.authService = authService;
        this.userService = userService;
        this.cookieService = cookieService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthResult result = authService.register(request);
        ResponseCookie jwtCookie = cookieService.createJwtCookie(result.token());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(result.response());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request);
        ResponseCookie jwtCookie = cookieService.createJwtCookie(result.token());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(result.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        ResponseCookie cleanCookie = cookieService.createCleanJwtCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(Map.of("message", "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User authenticatedUser = userService.getAuthenticatedUser();
        return ResponseEntity.ok(UserResponse.fromEntity(authenticatedUser));
    }
}
