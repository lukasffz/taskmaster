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

    public CookieService(@Value("${app.jwt.expiration}") long jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
    }

    public ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from(JWT_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS via profile/config
                .path("/")
                .maxAge(jwtExpiration / 1000)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie createCleanJwtCookie() {
        return ResponseCookie.from(JWT_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
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
