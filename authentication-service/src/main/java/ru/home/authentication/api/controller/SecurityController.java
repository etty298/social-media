package ru.home.authentication.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.home.authentication.api.dto.PasswordDto;
import ru.home.authentication.api.dto.SignInDto;
import ru.home.authentication.api.dto.SignUpDto;
import ru.home.authentication.api.dto.TokenValidationDto;
import ru.home.authentication.api.jwt.JwtToken;
import ru.home.authentication.kafka.producer.UserEventProducer;
import ru.home.authentication.store.entities.UserEntity;
import ru.home.authentication.store.repositories.UserRepository;

@RestController
@RequiredArgsConstructor
@Transactional
public class SecurityController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtToken jwtToken;

    private final UserEventProducer userEventProducer;



    @PostMapping("/auth/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpDto signUpDto) {
        if (userRepository.existsByUsername(signUpDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different username.");
        }
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different email.");
        }

        UserEntity user = new UserEntity();
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        userRepository.save(user);

        userEventProducer.sendUserRegisteredEvent(signUpDto.getUsername(),
                signUpDto.getName(), signUpDto.getEmail());

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInDto signInDto) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInDto.getUsername(), signInDto.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtToken.generateToken(authentication);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/settings/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody PasswordDto passwordDto) {
        String token = authHeader.substring(7);
        String username = jwtToken.getNameFromJwt(token);

        UserEntity user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User \"%s\" not found."));
        user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PostMapping("/auth/validate-token")
    public ResponseEntity<TokenValidationDto> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(TokenValidationDto.builder()
                        .valid(false)
                        .username(null)
                        .email(null)
                        .build());
            }

            String token = authHeader.substring(7);
            String username = jwtToken.getNameFromJwt(token);
            
            UserEntity user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(TokenValidationDto.builder()
                        .valid(false)
                        .username(null)
                        .email(null)
                        .build());
            }

            return ResponseEntity.ok(TokenValidationDto.builder()
                    .valid(true)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(TokenValidationDto.builder()
                    .valid(false)
                    .username(null)
                    .email(null)
                    .build());
        }
    }
}
