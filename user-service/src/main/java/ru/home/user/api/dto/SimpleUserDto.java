package ru.home.user.api.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleUserDto {
    private String username;
    private String name;
}
