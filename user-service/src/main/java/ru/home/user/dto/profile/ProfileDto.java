package ru.home.user.dto.profile;

import ru.home.user.dto.user.UserDto;

public record ProfileDto(
        UserDto user,
        StatsDto stats
) {
}
