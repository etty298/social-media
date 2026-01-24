package ru.home.user.controller.relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.home.user.dto.relationship.actions.FollowResultDto;
import ru.home.user.dto.relationship.actions.UnfollowResultDto;
import ru.home.user.service.relationship.ActionsService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/")
public class ActionsController {

    private final ActionsService actionsService;


    @PostMapping("/{userId}/follow")
    public ResponseEntity<FollowResultDto> follow(
            @RequestAttribute(name = "userId") UUID followerId,
            @PathVariable(name = "userId") UUID followingId) {

        return actionsService.follow(followerId, followingId);
    }

    @DeleteMapping("/{userId}/unfollow")
    public ResponseEntity<UnfollowResultDto> unfollow(
            @RequestAttribute(name = "userId") UUID followerId,
            @PathVariable(name = "userId") UUID followingId) {

        return actionsService.unfollow(followerId, followingId);
    }
}
