package ru.practicum.explorewithme.request.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.request.client.user.UserClientErrorDecoder;

@Configuration
@RequiredArgsConstructor
public class UserClientConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public Feign.Builder feignUserBuilder() {
        return Feign.builder()
                .errorDecoder(new UserClientErrorDecoder(objectMapper));
    }

}


