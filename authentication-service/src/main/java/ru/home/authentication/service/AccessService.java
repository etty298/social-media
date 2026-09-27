package ru.home.authentication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home.authentication.dto.access.LoginDto;
import ru.home.authentication.dto.access.RegisterDto;
import ru.home.authentication.dto.token.TokenResponseDto;
import ru.home.authentication.dto.user.UserDto;
import ru.home.authentication.entities.RefreshTokenEntity;
import ru.home.authentication.entities.Role;
import ru.home.authentication.entities.UserEntity;
import ru.home.authentication.exception.BadRequestException;
import ru.home.authentication.exception.NotFoundException;
import ru.home.authentication.exception.ServiceUnavailableException;
import ru.home.authentication.factory.UserDtoFactory;
import ru.home.authentication.kafka.producer.UserEventProducer;
import ru.home.authentication.repository.UserRepository;
import ru.home.authentication.security.jwt.JwtToken;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AccessService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtToken jwtToken;
    private final UserDtoFactory userDtoFactory;
    private final RefreshTokenService refreshTokenService;
    private final UserEventProducer userEventProducer;
    private final StringRedisTemplate redisTemplate;

    public ResponseEntity<UserDto> register(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.username())) {
            throw new BadRequestException("Choose different username");
        }
        if (userRepository.existsByEmail(registerDto.email())) {
            throw new BadRequestException("Choose different email");
        }

        UserEntity user = UserEntity.builder()
                .username(registerDto.username())
                .email(registerDto.email())
                .role(Role.USER)
                .enabled(true)
                .password(passwordEncoder.encode(registerDto.password()))
                .build();

        userRepository.saveAndFlush(user);

        userEventProducer.sendUserRegisteredEvent(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.OK).body(userDtoFactory.map(user));
    }

    public ResponseEntity<TokenResponseDto> login(LoginDto loginDto) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Wrong credentials");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserEntity user = userRepository.findByUsername(loginDto.username())
                .orElseThrow(() -> new NotFoundException("Wrong username"));
        String accessToken = jwtToken.generateToken(user);
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .secure(false)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(30))
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDto(user.getId(), accessToken, jwtToken.getJti(accessToken), jwtToken.getExpiration(accessToken)));
    }

    public ResponseEntity<Void> logout(String authHeader) {
        String accessToken = authHeader.substring(7);
        UUID userId = jwtToken.getUserId(accessToken);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        try {
            redisTemplate.opsForValue().set("blacklist:" + jwtToken.getJti(accessToken),
                    "revoked",
                    Duration.between(Instant.now(), jwtToken.getExpiration(accessToken)).toMillis(),
                    TimeUnit.MILLISECONDS);
        } catch (RedisConnectionFailureException e) {
            log.error("Cannot blacklist token: Redis unavailable. userId={}", userId, e);
            throw new ServiceUnavailableException("Logout unavailable, try again later");
        }

        refreshTokenService.revokeAll(user);

        SecurityContextHolder.clearContext();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public ResponseEntity<TokenResponseDto> refresh(String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenService.validateAndGet(refreshToken);
        String accessToken = jwtToken.generateToken(refreshTokenEntity.getUser());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshTokenEntity.getToken())
                .secure(false)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(30))
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDto(refreshTokenEntity.getUser().getId(), accessToken, jwtToken.getJti(accessToken), jwtToken.getExpiration(accessToken)));
    }
}
