package ru.home.user.dto.relationship.actions;

import java.time.Instant;
import java.util.UUID;

public record FollowResultDto(
        boolean success,
        UUID followerId,
        UUID followingId,
        boolean isMutual,
        Instant createdAt
) {}
