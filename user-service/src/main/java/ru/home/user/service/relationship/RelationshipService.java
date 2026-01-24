package ru.home.user.service.relationship;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.home.user.dto.relationship.status.RelationshipStatusDto;
import ru.home.user.exceptions.NotFoundException;
import ru.home.user.repositories.UserRelationshipRepository;
import ru.home.user.repositories.UserRepository;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RelationshipService {
    private final UserRelationshipRepository userRelationshipRepository;
    private final UserRepository userRepository;

    public ResponseEntity<RelationshipStatusDto> relationship(UUID userId, UUID targetUserId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        if (!userRepository.existsById(targetUserId)) {
            throw new NotFoundException("Target user not found");
        }

        boolean isFollowing = userRelationshipRepository
                .findByFollowerIdAndFollowingId(userId, targetUserId)
                .isPresent(); // userId подписан на targetUserId
        boolean isFollowedBy = userRelationshipRepository
                .findByFollowerIdAndFollowingId(targetUserId, userId)
                .isPresent(); // targetUserId подписан на userId
        boolean isFriend = userRelationshipRepository
                .areFriends(userId, targetUserId);

        return ResponseEntity.ok(new RelationshipStatusDto(
                userId,
                targetUserId,
                isFollowing,
                isFollowedBy,
                isFriend
        ));
    }
}
