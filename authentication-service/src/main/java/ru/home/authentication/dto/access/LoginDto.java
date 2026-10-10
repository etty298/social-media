package ru.home.authentication.dto.access;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDto(
        @Size(min = 4, message = "Username should be more than 4 symbols")
        @NotBlank(message = "Username can not be blank")
        String username,

        @Size(min = 6, message = "Password should be more than 6 symbols")
        @NotBlank(message = "Password can not be blank")
        String password
) {}
