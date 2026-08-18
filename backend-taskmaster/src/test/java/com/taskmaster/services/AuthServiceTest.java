package com.taskmaster.services;

import com.taskmaster.dtos.auth.LoginRequest;
import com.taskmaster.dtos.auth.RegisterRequest;
import com.taskmaster.exceptions.EmailAlreadyExistsException;
import com.taskmaster.models.Role;
import com.taskmaster.models.User;
import com.taskmaster.repositories.UserRepository;
import com.taskmaster.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Lukas User", "lukas@test.com", "password123");
        loginRequest = new LoginRequest("lukas@test.com", "password123");
        user = new User("Lukas User", "lukas@test.com", "encodedPassword", Role.USER);
        user.setId(1L);
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("mock.jwt.token");

        AuthService.AuthResult result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("mock.jwt.token", result.token());
        assertEquals("lukas@test.com", result.response().getEmail());
        assertEquals("Lukas User", result.response().getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringExistingEmail() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock.jwt.token");

        AuthService.AuthResult result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("mock.jwt.token", result.token());
        assertEquals("lukas@test.com", result.response().getEmail());
    }
}
