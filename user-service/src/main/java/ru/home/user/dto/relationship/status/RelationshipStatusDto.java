package ru.home.user.dto.relationship.status;

import java.util.UUID;

public record RelationshipStatusDto(
        UUID userId,
        UUID targetUserId,
        boolean isFollowing, // userId подписан на targetUserId
        boolean isFollowedBy, // targetUserId подписан на userId
        boolean isFriend
) {
}
