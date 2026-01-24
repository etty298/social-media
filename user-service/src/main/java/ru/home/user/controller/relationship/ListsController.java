package ru.home.user.controller.relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.home.user.dto.relationship.lists.FollowDto;
import ru.home.user.dto.relationship.lists.FriendDto;
import ru.home.user.dto.paging.PageResponseDto;
import ru.home.user.service.relationship.ListsService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class ListsController {

    private final ListsService listsService;

    @GetMapping("/{userId}/friends")
    public ResponseEntity<PageResponseDto<FriendDto>> friends(
            @PathVariable(name = "userId") UUID userId,
            Pageable pageable) {

        return listsService.friends(userId, pageable);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<PageResponseDto<FollowDto>> followers(
            @PathVariable(name = "userId") UUID userId,
            Pageable pageable) {

        return listsService.followers(userId, pageable);
    }

    @GetMapping("/{userId}/followings")
    public ResponseEntity<PageResponseDto<FollowDto>> followings(
            @PathVariable(name = "userId") UUID userId,
            Pageable pageable) {

        return listsService.followings(userId, pageable);
    }
}
