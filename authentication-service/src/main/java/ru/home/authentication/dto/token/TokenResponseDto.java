package ru.home.authentication.dto.token;

import java.time.Instant;
import java.util.UUID;

public record TokenResponseDto(
        UUID userId,
        String accessToken,
        Instant expiration

) {}
