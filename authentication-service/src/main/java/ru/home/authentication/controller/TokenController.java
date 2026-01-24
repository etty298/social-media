package ru.home.authentication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.home.authentication.dto.token.TokenValidationDto;
import ru.home.authentication.service.RefreshTokenService;
import ru.home.authentication.service.TokenService;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @GetMapping("/api/v1/token/validate")
    public ResponseEntity<TokenValidationDto> validate(@RequestHeader("Authorization") String authHeader) {
        return tokenService.validate(authHeader);
    }

    @DeleteMapping("/api/v1/auth/token")
    public ResponseEntity<Void> revoke(@CookieValue("refreshToken") String refreshToken) {
        return tokenService.revoke(refreshToken);
    }

    @DeleteMapping("/api/v1/auth/tokens")
    public ResponseEntity<Void> revokeAll(@CookieValue("refreshToken") String refreshToken) {
        return tokenService.revokeAll(refreshToken);
    }
}