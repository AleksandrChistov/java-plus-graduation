package ru.practicum.explorewithme.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.event.client.category.CategoryClientErrorDecoder;

@Configuration
public class CategoryClientConfig {
    @Bean
    public ErrorDecoder categoryErrorDecoder(ObjectMapper objectMapper) {
        return new CategoryClientErrorDecoder(objectMapper);
    }
}


