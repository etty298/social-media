package ru.home.authentication.dto.token;

import ru.home.authentication.entities.Role;

import java.util.UUID;

public record TokenValidationDto(boolean valid, UUID id, String jti, String username, Role role, String email) {}
