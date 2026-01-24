package ru.home.user.service.profile.current;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.user.dto.profile.ProfileDto;
import ru.home.user.dto.profile.StatsDto;
import ru.home.user.dto.profile.UpdateProfileDto;
import ru.home.user.entities.UserEntity;
import ru.home.user.exceptions.NotFoundException;
import ru.home.user.mapper.UserMapper;
import ru.home.user.repositories.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ResponseEntity<ProfileDto> getUser(UUID userId) {

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

    @Transactional
    public ResponseEntity<ProfileDto> changeUser(UUID userId, UpdateProfileDto updateProfileDto) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!updateProfileDto.name().trim().isEmpty()) {
            user.setName(updateProfileDto.name());
        }

        if (!updateProfileDto.bio().trim().isEmpty()) {
            user.setBio(updateProfileDto.bio());
        }

        userRepository.save(user);

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
