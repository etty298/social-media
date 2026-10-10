package ru.home.user.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileDto(

        @NotBlank(message = "Name can not be blank")
        String name,
        String bio
) {
}
