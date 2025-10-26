package ru.home.authentication.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private static final String TOPIC = "AUTHENTICATION_SERVICE";

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendUserRegisteredEvent(String username, String name,  String email) {
        sendEvent("user_registered", username, name, email);
    }

    private void sendEvent(String type, String username, String name, String email) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", type);
            event.put("email", email);
            event.put("name", name);
            if (username != null) {
                event.put("username", username);
            }

            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, json);
        } catch (Exception e) {
            log.error("Failed to send Kafka event of type: {} : {}", type, e.getMessage(), e);
        }
    }
}
