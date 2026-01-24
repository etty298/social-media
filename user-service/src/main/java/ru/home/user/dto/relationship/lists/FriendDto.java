package ru.home.user.dto.relationship.lists;

import ru.home.user.dto.user.UserDto;

import java.time.Instant;

public record FriendDto (
        UserDto user,
        Instant friendshipEstablishedAt
){}
