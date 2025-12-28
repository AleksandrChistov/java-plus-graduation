package ru.practicum.explorewithme.request.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.request.client.event.EventClientErrorDecoder;

@Configuration
public class EventClientConfig {
    @Bean
    public ErrorDecoder eventErrorDecoder(ObjectMapper objectMapper) {
        return new EventClientErrorDecoder(objectMapper);
    }
}


