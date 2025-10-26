package ru.home.user.api.factories;

import org.springframework.stereotype.Component;
import ru.home.user.api.dto.SimpleUserDto;
import ru.home.user.api.dto.UserDto;
import ru.home.user.store.entities.UserEntity;

import java.util.stream.Collectors;

@Component
public class UserDtoFactory {

    public UserDto createUserDto(UserEntity entity) {
        return UserDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .name(entity.getName())
                .friends(entity.getFriends().stream()
                        .map(this::createSimpleUserDto)
                        .collect(Collectors.toList()))
                .followers(entity.getFollowers().stream()
                        .map(this::createSimpleUserDto)
                        .collect(Collectors.toList()))
                .followings(entity.getFollowings().stream()
                        .map(this::createSimpleUserDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public SimpleUserDto createSimpleUserDto(UserEntity entity) {
        return SimpleUserDto.builder()
                .username(entity.getUsername())
                .name(entity.getName())
                .build();
    }
}
