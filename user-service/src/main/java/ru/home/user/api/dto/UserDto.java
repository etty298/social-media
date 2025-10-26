package ru.home.user.api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String name;
    private List<SimpleUserDto> friends;
    private List<SimpleUserDto> followers;
    private List<SimpleUserDto> followings;
}