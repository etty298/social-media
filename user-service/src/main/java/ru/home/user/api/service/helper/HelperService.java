package ru.home.user.api.service.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.home.user.api.exceptions.NotFoundException;
import ru.home.user.store.entities.UserEntity;
import ru.home.user.store.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class HelperService {

    private final UserRepository userRepository;

    public UserEntity getUserOrThrowException(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("User \"%s\" not found", username)));
    }
}
