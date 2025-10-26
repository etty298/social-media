package ru.home.authentication.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public NewTopic newTopic() {
        return new NewTopic("AUTHENTICATION_SERVICE", 1, (short) 1);
    }
}
