package ru.home.user.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.user.api.dto.UserDto;
import ru.home.user.api.exceptions.BadRequestException;
import ru.home.user.api.exceptions.NotFoundException;
import ru.home.user.api.factories.UserDtoFactory;
import ru.home.user.api.service.helper.HelperService;
import ru.home.user.kafka.producer.UserEventProducer;
import ru.home.user.store.entities.UserEntity;
import ru.home.user.store.repositories.UserRepository;

import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;

    private final UserDtoFactory userDtoFactory;

    private final HelperService helperService;

    private final UserEventProducer userEventProducer;


    public ResponseEntity<UserDto> getProfile(String username) {
        UserEntity user = helperService.getUserOrThrowException(username);
        return ResponseEntity.ok(userDtoFactory.createUserDto(user));
    }


    public ResponseEntity<?> updateProfile(String attributeUsername, String username, String action, Optional<String> optionalParam) {
        UserEntity user = helperService.getUserOrThrowException(attributeUsername);

        if (!user.getUsername().equals(username)) {
            throw new BadRequestException("You don't have permission");
        }

        switch (action.toLowerCase()) {
            case "change_username" -> optionalParam.ifPresentOrElse(param -> {
                userRepository.findByUsername(param).ifPresent(userEntity -> {
                    throw new BadRequestException("This username already taken");
                });
                user.setUsername(param);
                userEventProducer.sendUserChangedUsernameEvent(param, user.getEmail());
                }, () -> {
                throw new BadRequestException("Username can not be blank");
            });
            case "change_name" -> optionalParam.ifPresentOrElse(user::setName, () -> {
                        throw new BadRequestException("Name can not be blank");
            });
            case "delete_profile" -> {
                UserEntity deletedUser = userRepository
                        .findByUsernameEquals(attributeUsername)
                        .orElseThrow(() -> new NotFoundException(String.format("User \"%s\" not found", username)));
                deleteFriendsAndFollowersAndFollowings(deletedUser);
                userRepository.delete(deletedUser);
                userEventProducer.sendUserDeletedProfileEvent(deletedUser.getUsername());
                return ResponseEntity.ok(true);
            }
            default -> throw new BadRequestException("Unknown action: " + action);
        }

        userRepository.saveAndFlush(user);

        return ResponseEntity.ok(userDtoFactory.createUserDto(user));
    }

    private void deleteFriendsAndFollowersAndFollowings(UserEntity user) {
        for (UserEntity follower : new HashSet<>(user.getFollowers())) {
            follower.getFollowings().remove(user);
            user.getFollowers().remove(follower);
        }

        for (UserEntity following : new HashSet<>(user.getFollowings())) {
            following.getFollowers().remove(user);
            user.getFollowings().remove(following);
        }

        for (UserEntity friend : new HashSet<>(user.getFriends())) {
            friend.getFriends().remove(user);
            user.getFriends().remove(friend);
        }

        userRepository.save(user);
    }

}
