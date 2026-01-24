package ru.home.user.controller.profile.current;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.home.user.dto.profile.ProfileDto;
import ru.home.user.dto.profile.UpdateProfileDto;
import ru.home.user.service.profile.current.CurrentProfileService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class CurrentProfileController {

    private final CurrentProfileService currentProfileService;

    @GetMapping()
    public ResponseEntity<ProfileDto> getUser(
            @RequestAttribute(name = "userId") UUID userId) {

        return currentProfileService.getUser(userId);
    }

    @PatchMapping()
    public ResponseEntity<ProfileDto> changeUser(
            @RequestAttribute(name = "userId") UUID userId,
            @RequestBody UpdateProfileDto updateProfileDto) {

        return currentProfileService.changeUser(userId, updateProfileDto);
    }
}
