package ru.home.user.service.relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.user.dto.paging.PageResponseDto;
import ru.home.user.dto.user.UserDto;
import ru.home.user.dto.relationship.lists.*;
import ru.home.user.exceptions.NotFoundException;
import ru.home.user.repositories.UserRelationshipRepository;
import ru.home.user.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ListsService {

    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;

    public ResponseEntity<PageResponseDto<FriendDto>> friends(UUID userId, Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        Page<FriendProjection> page = userRelationshipRepository.findFriends(userId, pageable);

        List<FriendDto> content = page.getContent().stream()
                .map(projection -> new FriendDto(
                        new UserDto(
                                projection.id(),
                                projection.username(),
                                projection.name(),
                                projection.bio())
                        , projection.friendshipEstablishedAt()
                )).toList();
        return ResponseEntity.ok(new PageResponseDto<>(
                content,
                page.getTotalElements(),
                page.getTotalPages()
        ));

    }

    public ResponseEntity<PageResponseDto<FollowDto>> followers(UUID userId, Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        Page<FollowProjection> page = userRelationshipRepository.findFollowers(userId, pageable);

        List<FollowDto> content = getFollowDtoList(userId, page);

        return ResponseEntity.ok(new PageResponseDto<>(
                content,
                page.getTotalElements(),
                page.getTotalPages()
        ));
    }

    public ResponseEntity<PageResponseDto<FollowDto>> followings(UUID userId, Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        Page<FollowProjection> page = userRelationshipRepository.findFollowings(userId, pageable);

        List<FollowDto> content = getFollowDtoList(userId, page);

        return ResponseEntity.ok(new PageResponseDto<>(
                content,
                page.getTotalElements(),
                page.getTotalPages()
        ));
    }

    private List<FollowDto> getFollowDtoList(UUID userId, Page<FollowProjection> page) {
        return page.getContent().stream()
                .map(projection -> new FollowDto(
                        new UserDto(
                                projection.id(),
                                projection.username(),
                                projection.name(),
                                projection.bio()),
                        projection.followedAt(),
                        userRelationshipRepository.areFriends(userId, projection.id())
                )).toList();
    }
}
