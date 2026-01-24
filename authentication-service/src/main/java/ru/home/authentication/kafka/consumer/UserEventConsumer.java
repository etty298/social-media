package ru.home.authentication.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.home.authentication.entities.UserEntity;
import ru.home.authentication.repository.UserRepository;

@Log4j2
//@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserRepository userRepository;

    //@KafkaListener(topics = "USER_SERVICE", groupId = "consumer")
    public void consume(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("type").asText();
            String email = event.get("email").asText();
            String username = event.has("username") ? event.get("username").asText() : "";
            UserEntity user;
            switch (eventType) {
                case "user_changed_username":
                    user = userRepository.findByEmail(email).orElseThrow();
                    user.setUsername(username);
                    userRepository.saveAndFlush(user);
                    break;
                case "user_deleted_profile":
                    user = userRepository.findByUsername(username).orElseThrow();
                    userRepository.delete(user);
                default:
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }
}
