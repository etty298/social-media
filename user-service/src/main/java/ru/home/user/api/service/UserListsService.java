package ru.home.user.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.user.api.dto.SimpleUserDto;
import ru.home.user.api.factories.UserDtoFactory;
import ru.home.user.api.service.helper.HelperService;
import ru.home.user.store.entities.UserEntity;
import ru.home.user.store.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class UserListsService {

    private final UserRepository userRepository;

    private final UserDtoFactory userDtoFactory;

    private final HelperService helperService;

    public ResponseEntity<List<SimpleUserDto>> getProfiles(Optional<String> optionalPrefixUsername,
                                                           Optional<String> optionalPrefixName) {
        Stream<UserEntity> usersStream = optionalPrefixName
                .filter(prefixName -> !prefixName.trim().isEmpty())
                .map(userRepository::streamAllByNameStartsWithIgnoreCase)
                .orElse(optionalPrefixUsername
                        .filter(prefixUsername -> !prefixUsername.trim().isEmpty())
                        .map(userRepository::streamAllByUsernameStartsWithIgnoreCase)
                        .orElseGet(userRepository::streamAllBy));

        return ResponseEntity.ok(usersStream
                .map(userDtoFactory::createSimpleUserDto)
                .collect(Collectors.toList()));
    }

    public ResponseEntity<List<SimpleUserDto>> getFriends(String username,
                                          Optional<String> optionalPrefixName) {
        UserEntity user = helperService.getUserOrThrowException(username);

        Stream<UserEntity> friendsStream = optionalPrefixName
                .filter(prefix -> !prefix.trim().isEmpty())
                .map(prefix -> user.getFriends().stream()
                        .filter(f -> f
                                .getName().toLowerCase()
                                .startsWith(prefix.toLowerCase())))
                .orElseGet(() -> user.getFriends().stream());

        return ResponseEntity.ok(friendsStream
                .map(userDtoFactory::createSimpleUserDto)
                .collect(Collectors.toList()));
    }

    public ResponseEntity<List<SimpleUserDto>> getFollowers(String username,
                                            Optional<String> optionalPrefixName) {
        UserEntity user = helperService.getUserOrThrowException(username);

        Stream<UserEntity> followersStream = optionalPrefixName
                .filter(prefix -> !prefix.trim().isEmpty())
                .map(prefix -> user.getFollowers().stream()
                        .filter(f -> f
                                .getName().toLowerCase()
                                .startsWith(prefix.toLowerCase())))
                .orElseGet(() -> user.getFollowers().stream());

        return ResponseEntity.ok(followersStream
                .map(userDtoFactory::createSimpleUserDto)
                .collect(Collectors.toList()));
    }

    public ResponseEntity<List<SimpleUserDto>> getFollowings(String username,
                                             Optional<String> optionalPrefixName) {
        UserEntity user = helperService.getUserOrThrowException(username);

        Stream<UserEntity> followingsStream = optionalPrefixName
                .filter(prefix -> !prefix.trim().isEmpty())
                .map(prefix -> user.getFollowings().stream()
                        .filter(f -> f
                                .getName().toLowerCase()
                                .startsWith(prefix.toLowerCase())))
                .orElseGet(() -> user.getFollowings().stream());

        return ResponseEntity.ok(followingsStream
                .map(userDtoFactory::createSimpleUserDto)
                .collect(Collectors.toList()));
    }
}
