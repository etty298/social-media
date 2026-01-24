package ru.home.user.service.profile.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.user.dto.paging.PageResponseDto;
import ru.home.user.dto.user.UserDto;
import ru.home.user.dto.profile.ProfileDto;
import ru.home.user.dto.profile.StatsDto;
import ru.home.user.entities.UserEntity;
import ru.home.user.exceptions.NotFoundException;
import ru.home.user.mapper.UserMapper;
import ru.home.user.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public ResponseEntity<PageResponseDto<UserDto>> getAllUsers(Optional<String> q, Pageable pageable) {

        Page<UserEntity> page = q
                .filter(username -> !username.trim().isEmpty())
                .map(username -> userRepository.findAllByUsernameStartsWithIgnoreCase(username, pageable))
                .orElseGet(() -> userRepository.findAllBy(pageable));

        List<UserDto> content = page.getContent().stream()
                .map(userMapper::toUserDto)
                .toList();

        return ResponseEntity.ok(new PageResponseDto<>(
                content,
                page.getTotalElements(),
                page.getTotalPages()
        ));
    }

    public ResponseEntity<ProfileDto> getUserByUserId(UUID userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.ok(new ProfileDto(
                userMapper.toUserDto(user),
                new StatsDto(
                        user.getFollowersCount(),
                        user.getFollowingsCount(),
                        user.getFriendsCount()
                )
        ));
    }

    public ResponseEntity<ProfileDto> getUserByUsername(String username) {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.ok(new ProfileDto(
                userMapper.toUserDto(user),
                new StatsDto(
                        user.getFollowersCount(),
                        user.getFollowingsCount(),
                        user.getFriendsCount()
                )
        ));
    }
}
