package ru.home.user.dto.relationship.lists;

import java.time.Instant;
import java.util.UUID;

public record FriendProjection(
        UUID id,
        String username,
        String name,
        String bio,
        Instant friendshipEstablishedAt
) {
}
