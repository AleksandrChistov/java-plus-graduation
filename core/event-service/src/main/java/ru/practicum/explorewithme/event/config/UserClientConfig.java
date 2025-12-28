package ru.practicum.explorewithme.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.event.client.user.UserClientErrorDecoder;

@Configuration
public class UserClientConfig {
    @Bean
    public ErrorDecoder userErrorDecoder(ObjectMapper objectMapper) {
        return new UserClientErrorDecoder(objectMapper);
    }
}


