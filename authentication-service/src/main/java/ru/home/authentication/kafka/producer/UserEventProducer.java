package ru.home.authentication.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.home.authentication.entities.Role;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private static final String TOPIC = "AUTHENTICATION_SERVICE";

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendUserRegisteredEvent(UUID id, String username, Role role, Instant createdAt) {
        sendEvent("user_registered", id, username, role, createdAt);
    }

    private void sendEvent(String type, UUID id, String username, Role role, Instant createdAt) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", type);
            event.put("id", id);
            event.put("username", username);
            event.put("role", role);
            event.put("createdAt", createdAt.toString());

            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, json);
            System.out.println(json);
        } catch (Exception e) {
            log.error("Failed to send Kafka event of type: {} : {}", type, e.getMessage(), e);
        }
    }
}
