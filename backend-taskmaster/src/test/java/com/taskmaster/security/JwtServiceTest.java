package com.taskmaster.security;

import com.taskmaster.models.Role;
import com.taskmaster.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        long testExpiration = 3600000; // 1 hour
        jwtService = new JwtService(testSecret, testExpiration);

        testUser = new User("Lukas Test", "lukas@test.com", "encodedPassword", Role.USER);
        testUser.setId(1L);
    }

    @Test
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test
    void shouldExtractUsernameCorrectly() {
        String token = jwtService.generateToken(testUser);
        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(testUser.getEmail(), extractedUsername);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token", testUser));
    }
}
