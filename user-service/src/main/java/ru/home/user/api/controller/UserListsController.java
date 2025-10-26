package ru.home.user.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.home.user.api.dto.SimpleUserDto;
import ru.home.user.api.service.UserListsService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserListsController {

    private final UserListsService userListsService;

    private final static String GET_PROFILES = "api/users";
    private final static String GET_FRIENDS = "api/users/{username}/friends";
    private final static String GET_FOLLOWERS = "api/users/{username}/followers";
    private final static String GET_FOLLOWINGS = "api/users/{username}/followings";

    @GetMapping(GET_PROFILES)
    public ResponseEntity<List<SimpleUserDto>> getProfiles(
            @RequestParam(name = "prefix_username", required = false) Optional<String> optionalPrefixUsername,
            @RequestParam(name = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        return userListsService.getProfiles(optionalPrefixUsername, optionalPrefixName);
    }

    @GetMapping(GET_FRIENDS)
    public ResponseEntity<List<SimpleUserDto>> getFriends(
            @PathVariable(name = "username") String username,
            @RequestParam(name = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        return userListsService.getFriends(username, optionalPrefixName);
    }

    @GetMapping(GET_FOLLOWERS)
    public ResponseEntity<List<SimpleUserDto>> getFollowers(
            @PathVariable(name = "username") String username,
            @RequestParam(name = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        return userListsService.getFollowers(username, optionalPrefixName);
    }

    @GetMapping(GET_FOLLOWINGS)
    public ResponseEntity<List<SimpleUserDto>> findAccounts(
            @PathVariable(name = "username") String username,
            @RequestParam(name = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        return userListsService.getFollowings(username, optionalPrefixName);
    }
}
