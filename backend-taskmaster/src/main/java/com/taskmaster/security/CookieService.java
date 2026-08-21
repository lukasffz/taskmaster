package com.taskmaster.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class CookieService {

    public static final String JWT_COOKIE_NAME = "taskmaster_token";

    private final long jwtExpiration;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public CookieService(
            @Value("${app.jwt.expiration}") long jwtExpiration,
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins
    ) {
        this.jwtExpiration = jwtExpiration;
        boolean isHttpsProd = allowedOrigins.startsWith("https://");
        this.cookieSecure = isHttpsProd;
        this.cookieSameSite = isHttpsProd ? "None" : "Lax";
    }

    public ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from(JWT_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(jwtExpiration / 1000)
                .sameSite(cookieSameSite)
                .build();
    }

    public ResponseCookie createCleanJwtCookie() {
        return ResponseCookie.from(JWT_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
    }

    public Optional<String> extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> JWT_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(token -> token != null && !token.isBlank())
                .findFirst();
    }
}
