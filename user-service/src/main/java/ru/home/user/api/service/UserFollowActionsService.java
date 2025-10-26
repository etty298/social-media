package ru.home.user.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.user.api.dto.UserDto;
import ru.home.user.api.exceptions.BadRequestException;
import ru.home.user.api.factories.UserDtoFactory;
import ru.home.user.api.service.helper.HelperService;
import ru.home.user.store.entities.UserEntity;
import ru.home.user.store.repositories.UserRepository;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UserFollowActionsService {

    private final UserRepository userRepository;

    private final UserDtoFactory userDtoFactory;

    private final HelperService helperService;


    public ResponseEntity<UserDto> follow(String username, String targetUsername) {

        if (username.equals(targetUsername)) {
            throw new BadRequestException("You can not follow yourself");
        }

        UserEntity targetUser = helperService.getUserOrThrowException(targetUsername);
        UserEntity user = helperService.getUserOrThrowException(username);

        if (user.getFriends().contains(targetUser) || targetUser.getFriends().contains(user)) {
            throw new BadRequestException("You are already friends");
        }

        if (user.getFollowings().contains(targetUser) || targetUser.getFollowers().contains(user)) {
            throw new BadRequestException(String.format("You are already following \"%s\"", targetUsername));
        }

        Set<UserEntity> followingList = user.getFollowings();
        Set<UserEntity> followersList = targetUser.getFollowers();

        if (targetUser.getFollowings().contains(user)) {
            Set<UserEntity> usersFriends = user.getFriends();
            usersFriends.add(targetUser);
            user.setFriends(usersFriends);

            Set<UserEntity> targetUsersFriends = targetUser.getFriends();
            targetUsersFriends.add(user);
            targetUser.setFriends(targetUsersFriends);

            Set<UserEntity> targetUsersFollowings = targetUser.getFollowings();
            Set<UserEntity> usersFollowersList = user.getFollowers();

            usersFollowersList.remove(targetUser);
            user.setFollowers(usersFollowersList);

            targetUsersFollowings.remove(user);
            targetUser.setFollowings(targetUsersFollowings);

            userRepository.saveAndFlush(user);
            userRepository.saveAndFlush(targetUser);

            return ResponseEntity.ok(userDtoFactory.createUserDto(user));
        }


        followingList.add(targetUser);
        user.setFollowings(followingList);

        followersList.add(user);
        targetUser.setFollowers(followersList);

        userRepository.saveAndFlush(user);
        userRepository.saveAndFlush(targetUser);

        return ResponseEntity.ok(userDtoFactory.createUserDto(user));
    }

    public ResponseEntity<UserDto> unfollow(String username, String targetUsername) {

        if (username.equals(targetUsername)) {
            throw new BadRequestException("You can not unfollow yourself");
        }

        UserEntity targetUser = helperService.getUserOrThrowException(targetUsername);
        UserEntity user = helperService.getUserOrThrowException(username);

        if (user.getFriends().contains(targetUser) || targetUser.getFriends().contains(user)) {
            Set<UserEntity> usersFriendsList = user.getFriends();
            usersFriendsList.remove(targetUser);
            user.setFriends(usersFriendsList);

            Set<UserEntity> targetFriendsList = targetUser.getFriends();
            targetFriendsList.remove(user);
            targetUser.setFriends(targetFriendsList);

            Set<UserEntity> usersFollowersList = user.getFollowers();
            usersFollowersList.add(targetUser);
            user.setFollowers(usersFollowersList);

            Set<UserEntity> targetFollowingList = targetUser.getFollowings();
            targetFollowingList.add(user);
            targetUser.setFollowings(targetFollowingList);

            userRepository.saveAndFlush(user);
            userRepository.saveAndFlush(targetUser);

            return ResponseEntity.ok(userDtoFactory.createUserDto(user));
        }

        if (!user.getFollowings().contains(targetUser) || !targetUser.getFollowers().contains(user)) {
            throw new BadRequestException(String.format("You are not following \"%s\"", targetUsername));
        }

        Set<UserEntity> usersFollowingsList = user.getFollowings();
        usersFollowingsList.remove(targetUser);
        user.setFollowings(usersFollowingsList);

        Set<UserEntity> targetFollowersList = targetUser.getFollowers();
        targetFollowersList.remove(user);
        targetUser.setFollowers(targetFollowersList);

        userRepository.saveAndFlush(user);
        userRepository.saveAndFlush(targetUser);

        return ResponseEntity.ok(userDtoFactory.createUserDto(user));
    }
}
