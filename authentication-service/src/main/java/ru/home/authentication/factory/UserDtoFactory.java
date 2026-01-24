package ru.home.authentication.factory;

import org.springframework.stereotype.Component;
import ru.home.authentication.dto.user.UserDto;
import ru.home.authentication.entities.UserEntity;

@Component
public class UserDtoFactory {

    public UserDto map(UserEntity entity) {
        return new UserDto(entity.getId(), entity.getUsername(), entity.getEmail(), entity.getRole(), entity.isEnabled());
    }
}
