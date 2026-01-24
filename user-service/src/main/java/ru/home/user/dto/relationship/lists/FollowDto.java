package ru.home.user.dto.relationship.lists;

import ru.home.user.dto.user.UserDto;

import java.time.Instant;

public record FollowDto(
        UserDto user,
        Instant followedAt,
        boolean isMutual
) {
}
