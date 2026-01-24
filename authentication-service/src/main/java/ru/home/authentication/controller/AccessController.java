package ru.home.authentication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.home.authentication.dto.access.LoginDto;
import ru.home.authentication.dto.access.RegisterDto;
import ru.home.authentication.dto.token.TokenResponseDto;
import ru.home.authentication.dto.user.UserDto;
import ru.home.authentication.service.AccessService;

@RestController
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;

    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterDto registerDto) {
        return accessService.register(registerDto);
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginDto loginDto) {
        return accessService.login(loginDto);
    }

    @PostMapping("/api/v1/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        return accessService.logout(authHeader);
    }

    @PostMapping("/api/v1/auth/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@CookieValue("refreshToken") String refreshToken) {
        return accessService.refresh(refreshToken);
    }

}
