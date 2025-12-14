package ru.practicum.explorewithme.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.event.client.category.CategoryClientErrorDecoder;

@Configuration
@RequiredArgsConstructor
public class CategoryClientConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public Feign.Builder feignCategoryBuilder() {
        return Feign.builder()
                .errorDecoder(new CategoryClientErrorDecoder(objectMapper));
    }

}


