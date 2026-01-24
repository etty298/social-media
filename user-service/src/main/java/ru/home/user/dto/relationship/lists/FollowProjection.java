package ru.home.user.dto.relationship.lists;

import java.time.Instant;
import java.util.UUID;

public record FollowProjection(
        UUID id,
        String username,
        String name,
        String bio,
        Instant followedAt
) {
}
