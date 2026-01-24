package ru.home.user.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.home.user.entities.Role;
import ru.home.user.exceptions.BadRequestException;
import ru.home.user.entities.UserEntity;
import ru.home.user.repositories.UserRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserRepository userRepository;

    @Transactional
    @KafkaListener(topics = "AUTHENTICATION_SERVICE", groupId = "consumer")
    public void consume(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("type").asText();
            UUID id = UUID.fromString(event.get("id").asText());
            String username = event.get("username").asText();
            Role role = Role.valueOf(event.get("role").asText());
            Instant createdAt = Instant.parse(event.get("createdAt").asText());
            switch (eventType) {
                case "user_registered":
                    userRepository.findByUsername(username)
                            .ifPresent(entity -> {
                                throw new BadRequestException("User already registered.");
                            });
                    userRepository.save(
                            UserEntity.builder()
                                    .id(id)
                                    .username(username)
                                    .role(role)
                                    .createdAt(createdAt)
                                    .build()
                    );
                    break;
                default:
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }
}
