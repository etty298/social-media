package ru.home.authentication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.home.authentication.dto.security.PasswordChangeDto;
import ru.home.authentication.service.SecurityService;

@RestController
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityService securityService;

    @PatchMapping("/api/v1/security/change-password")
    public ResponseEntity<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                         @Valid @RequestBody PasswordChangeDto passwordChangeDto) {
        return securityService.changePassword(authHeader, passwordChangeDto);
    }
}
