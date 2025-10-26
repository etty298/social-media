package ru.home.user.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.home.user.api.exceptions.BadRequestException;
import ru.home.user.store.entities.UserEntity;
import ru.home.user.store.repositories.UserRepository;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserRepository userRepository;

    @KafkaListener(topics = "AUTHENTICATION_SERVICE", groupId = "consumer")
    public void consume(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("type").asText();
            String email = event.get("email").asText();
            String name = event.get("name").asText();
            String username = event.has("username") ? event.get("username").asText() : "";
            switch (eventType) {
                case "user_registered":
                    userRepository.findByUsername(username)
                            .ifPresent(entity -> {
                                throw new BadRequestException("User already registered.");
                            });
                    userRepository.saveAndFlush(
                            UserEntity.builder()
                                    .username(username)
                                    .name(name)
                                    .email(email)
                                    .build());
                    break;
                default:
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }
}
