package ru.home.user.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.home.user.api.dto.UserDto;
import ru.home.user.api.exceptions.BadRequestException;
import ru.home.user.api.service.UserFollowActionsService;

@RestController
@RequiredArgsConstructor
public class UserFollowActionsController {

    private final UserFollowActionsService userFollowActionsService;

    private final static String FOLLOW_ACTIONS = "api/users/{username}";


    @PostMapping(FOLLOW_ACTIONS)
    public ResponseEntity<UserDto> follow(
            @RequestAttribute(name = "username") String username,
            @PathVariable(name = "username") String targetUsername,
            @RequestParam(name = "action") String action) {

        ResponseEntity<UserDto> result;
        switch (action.toLowerCase()) {
            case "follow" -> result = userFollowActionsService.follow(username, targetUsername);
            case "unfollow" -> result = userFollowActionsService.unfollow(username, targetUsername);
            default -> throw new BadRequestException("Unknown action: " + action);
        }
        return result;
    }
}
