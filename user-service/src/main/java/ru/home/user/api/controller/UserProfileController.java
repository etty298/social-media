package ru.home.user.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.home.user.api.dto.UserDto;
import ru.home.user.api.service.UserProfileService;

import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    private final static String GET_PROFILE = "api/users/{username}";
    private final static String UPDATE_PROFILE = "api/users/{username}";


    @GetMapping(GET_PROFILE)
    public ResponseEntity<UserDto> getProfile(
            @PathVariable(name = "username") String username) {

        return userProfileService.getProfile(username);
    }

    @PatchMapping(UPDATE_PROFILE)
    public ResponseEntity<?> updateProfile(
            @RequestAttribute(name = "username") String attributeUsername,
            @PathVariable(name = "username") String username,
            @RequestParam(name = "action") String action,
            @RequestParam(name = "param", required = false) Optional<String> optionalParam) {

        return userProfileService.updateProfile(attributeUsername, username, action, optionalParam);
    }
}
