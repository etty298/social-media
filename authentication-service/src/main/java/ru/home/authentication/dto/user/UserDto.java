package ru.home.authentication.dto.user;

import ru.home.authentication.entities.Role;

import java.util.UUID;

public record UserDto (UUID id, String username, String email, Role role, boolean enabled) {}
