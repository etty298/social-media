package ru.home.user.service.relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.user.dto.relationship.actions.FollowResultDto;
import ru.home.user.dto.relationship.actions.UnfollowResultDto;
import ru.home.user.entities.UserRelationshipEntity;
import ru.home.user.exceptions.BadRequestException;
import ru.home.user.exceptions.NotFoundException;
import ru.home.user.repositories.UserRelationshipRepository;
import ru.home.user.repositories.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ActionsService {

    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;


    public ResponseEntity<FollowResultDto> follow(UUID followerId, UUID followingId) {

        if (followerId.equals(followingId)) {
            throw new BadRequestException("Cannot follow yourself");
        }

        if (!userRepository.existsById(followerId)) {
            throw new NotFoundException("Follower user not found");
        }

        if (!userRepository.existsById(followingId)) {
            throw new NotFoundException("Following user not found");
        }

        if (userRelationshipRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BadRequestException("Already following this user");
        }

        // Создание связи
        UserRelationshipEntity relationship = UserRelationshipEntity.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();

        userRelationshipRepository.save(relationship);

        // Обновление счетчиков
        incrementFollowingsCount(followerId);
        incrementFollowersCount(followingId);

        boolean areFriends = userRelationshipRepository.areFriends(followerId, followingId);

        if (areFriends) {
            incrementFriendsCount(followerId);
            incrementFriendsCount(followingId);
        }

        return ResponseEntity.ok(new FollowResultDto(true, followerId, followingId, areFriends, Instant.now()));
    }

    public ResponseEntity<UnfollowResultDto> unfollow(UUID followerId, UUID followingId) {

        if (followerId.equals(followingId)) {
            throw new BadRequestException("Cannot unfollow yourself");
        }

        if (!userRepository.existsById(followerId)) {
            throw new NotFoundException("Follower user not found");
        }

        if (!userRepository.existsById(followingId)) {
            throw new NotFoundException("Unfollowing user not found");
        }

        if (!userRelationshipRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BadRequestException("You are not following this user");
        }

        // Удаление связи
        userRelationshipRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);

        // Обновление счетчиков
        decrementFollowingsCount(followerId);
        decrementFollowersCount(followingId);

        boolean areFriends = userRelationshipRepository.areFriends(followerId, followingId);

        if (areFriends) {
            decrementFriendsCount(followerId);
            decrementFriendsCount(followingId);
        }

        return ResponseEntity.ok(new UnfollowResultDto(true, followingId, areFriends));
    }


    private void incrementFollowersCount(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFollowersCount(user.getFollowersCount() + 1);
            userRepository.save(user);
        });
    }

    private void decrementFollowersCount(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFollowersCount(Math.max(0, user.getFollowersCount() - 1));
            userRepository.save(user);
        });
    }

    private void incrementFollowingsCount(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFollowingsCount(user.getFollowingsCount() + 1);
            userRepository.save(user);
        });
    }

    private void decrementFollowingsCount(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFollowingsCount(Math.max(0, user.getFollowingsCount() - 1));
            userRepository.save(user);
        });
    }

    private void incrementFriendsCount(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFriendsCount(user.getFriendsCount() + 1);
            userRepository.save(user);
        });
    }

    private void decrementFriendsCount(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFriendsCount(Math.max(0, user.getFriendsCount() - 1));
            userRepository.save(user);
        });
    }


}
