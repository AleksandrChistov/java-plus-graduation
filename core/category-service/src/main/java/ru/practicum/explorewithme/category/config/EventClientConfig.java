package ru.practicum.explorewithme.category.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.category.client.event.EventClientErrorDecoder;

@Configuration
@RequiredArgsConstructor
public class EventClientConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public Feign.Builder feignEventBuilder() {
        return Feign.builder()
                .errorDecoder(new EventClientErrorDecoder(objectMapper));
    }

}


