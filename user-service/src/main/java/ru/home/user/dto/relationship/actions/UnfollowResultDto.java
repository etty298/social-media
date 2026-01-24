package ru.home.user.dto.relationship.actions;

import java.util.UUID;

public record UnfollowResultDto(
        boolean success,
        UUID unfollowedId,
        boolean wasMutual
) {
}
