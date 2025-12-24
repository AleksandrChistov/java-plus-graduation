package ru.practicum.explorewithme.category.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.category.client.event.EventClientErrorDecoder;

@Configuration
public class EventClientConfig {
    @Bean
    public ErrorDecoder eventErrorDecoder(ObjectMapper objectMapper) {
        return new EventClientErrorDecoder(objectMapper);
    }
}


