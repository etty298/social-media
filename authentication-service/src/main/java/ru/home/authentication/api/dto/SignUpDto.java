package ru.home.authentication.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpDto {

    private String username;

    private String name;

    private String password;

    private String email;
}
