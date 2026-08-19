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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Registration, login, logout and current-user operations")
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
        @Operation(summary = "Register a user")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
        })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthResult result = authService.register(request);
        ResponseCookie jwtCookie = cookieService.createJwtCookie(result.token());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(result.response());
    }

    @PostMapping("/login")
        @Operation(summary = "Authenticate a user")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful; JWT is sent as an HttpOnly cookie"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
        })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request);
        ResponseCookie jwtCookie = cookieService.createJwtCookie(result.token());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(result.response());
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out the current user")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    public ResponseEntity<Map<String, String>> logout() {
        ResponseCookie cleanCookie = cookieService.createCleanJwtCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(Map.of("message", "Logout successful"));
    }

    @GetMapping("/me")
        @Operation(summary = "Get the authenticated user")
        @SecurityRequirement(name = "cookieAuth")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated user returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
        })
    public ResponseEntity<UserResponse> getCurrentUser() {
        User authenticatedUser = userService.getAuthenticatedUser();
        return ResponseEntity.ok(UserResponse.fromEntity(authenticatedUser));
    }
}
