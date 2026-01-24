package ru.home.user.dto.profile;

public record StatsDto(
        Long followersCount,
        Long followingsCount,
        Long friendsCount
) {
}
