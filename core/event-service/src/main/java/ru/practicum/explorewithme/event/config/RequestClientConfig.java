package ru.practicum.explorewithme.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.event.client.request.RequestClientErrorDecoder;

@Configuration
public class RequestClientConfig {
    @Bean
    public ErrorDecoder requestErrorDecoder(ObjectMapper objectMapper) {
        return new RequestClientErrorDecoder(objectMapper);
    }
}


