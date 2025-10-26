package ru.home.user.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationDto {
    private boolean valid;
    private String username;
    private String email;
}
