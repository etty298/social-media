package ru.home.user.dto.user;

import java.util.UUID;

/**
 * DTO for {@link ru.home.user.entities.UserEntity}
 */
public record UserDto(
        UUID id,
        String username,
        String name,
        String bio
) {}