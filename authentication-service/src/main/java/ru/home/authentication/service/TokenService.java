package ru.home.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.authentication.dto.token.TokenValidationDto;
import ru.home.authentication.entities.RefreshTokenEntity;
import ru.home.authentication.entities.UserEntity;
import ru.home.authentication.exception.BadRequestException;
import ru.home.authentication.repository.RefreshTokenRepository;
import ru.home.authentication.repository.UserRepository;
import ru.home.authentication.security.jwt.JwtToken;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenService {

    private final JwtToken jwtToken;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public ResponseEntity<TokenValidationDto> validate(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(new TokenValidationDto(false, null, null, null, null));
            }

            String token = authHeader.substring(7);
            UUID userId = jwtToken.getUserId(token);

            UserEntity user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.ok(new TokenValidationDto(false, null, null, null, null));
            }

            return ResponseEntity.ok(new TokenValidationDto(true, user.getId(), user.getUsername(), user.getRole(), user.getEmail()));

        } catch (Exception e) {
            return ResponseEntity.ok(new TokenValidationDto(false, null, null, null, null));
        }
    }

    public ResponseEntity<Void> revoke(String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = findRefreshTokenOrThrowException(refreshToken);

        refreshTokenEntity.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshTokenEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }



    public ResponseEntity<Void> revokeAll(String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = findRefreshTokenOrThrowException(refreshToken);
        UserEntity user = refreshTokenEntity.getUser();
        refreshTokenRepository.revokeAllUserTokens(user, Instant.now());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private RefreshTokenEntity findRefreshTokenOrThrowException(String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshTokenEntity.isRevoked()) {
            throw new BadRequestException("Refresh token has already been revoked");
        }

        if (refreshTokenEntity.isExpired()) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new BadRequestException("Refresh token has expired");
        }

        return refreshTokenEntity;
    }
}
