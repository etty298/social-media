package ru.home.user.mapper;

import org.springframework.stereotype.Component;
import ru.home.user.dto.user.UserDto;
import ru.home.user.entities.UserEntity;

import java.util.UUID;

@Component
public class UserMapper {
    public UserDto toUserDto(UserEntity userEntity) {
        UUID userEntityId = userEntity.getId();
        String userEntityUsername = userEntity.getUsername();
        String userEntityName = userEntity.getName();
        String userEntityBio = userEntity.getBio();
        UserDto userDto = new UserDto(
                userEntityId,
                userEntityUsername,
                userEntityName,
                userEntityBio
        );
        return userDto;
    }

    public UserEntity toEntity(UserDto userDto) {
        UUID userDtoId = userDto.id();
        String userDtoUsername = userDto.username();
        String userDtoName = userDto.name();
        String userDtoBio = userDto.bio();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userDtoId);
        userEntity.setUsername(userDtoUsername);
        userEntity.setName(userDtoName);
        userEntity.setBio(userDtoBio);
        return userEntity;
    }
}