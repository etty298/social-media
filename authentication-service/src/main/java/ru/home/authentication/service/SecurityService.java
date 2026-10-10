package ru.home.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.authentication.dto.security.PasswordChangeDto;
import ru.home.authentication.entities.UserEntity;
import ru.home.authentication.exception.BadRequestException;
import ru.home.authentication.repository.UserRepository;
import ru.home.authentication.security.jwt.JwtToken;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final JwtToken jwtToken;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<Void> changePassword(String authHeader, PasswordChangeDto passwordChangeDto) {
        String accessToken = authHeader.substring(7);
        UUID userId = jwtToken.getUserId(accessToken);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (passwordEncoder.matches(passwordChangeDto.oldPassword(), user.getPassword())) {
            if (passwordEncoder.matches(passwordChangeDto.newPassword(), user.getPassword())) {
                throw new BadRequestException("Password must be different");
            }
            user.setPassword(passwordEncoder.encode(passwordChangeDto.newPassword()));
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
