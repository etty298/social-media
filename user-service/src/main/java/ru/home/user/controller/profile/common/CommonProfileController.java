package ru.home.user.controller.profile.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.home.user.dto.paging.PageResponseDto;
import ru.home.user.dto.user.UserDto;
import ru.home.user.dto.profile.ProfileDto;
import ru.home.user.service.profile.common.CommonProfileService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class CommonProfileController {

    private final CommonProfileService commonProfileService;

    @GetMapping()
    public ResponseEntity<PageResponseDto<UserDto>> getAllUsers(
            @RequestParam(value = "q", required = false) Optional<String> q,
            Pageable pageable) {

        return commonProfileService.getAllUsers(q, pageable);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ProfileDto> getUserByIdentifier(
            @PathVariable(value = "identifier") String identifier) {

        // Проверяем, является ли identifier UUID
        try {
            UUID userId = UUID.fromString(identifier);
            return commonProfileService.getUserByUserId(userId);
        } catch (IllegalArgumentException e) {
            // Если не UUID, значит это username
            return commonProfileService.getUserByUsername(identifier);
        }
    }
}
