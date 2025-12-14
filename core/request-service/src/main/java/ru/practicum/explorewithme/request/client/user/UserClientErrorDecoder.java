package ru.practicum.explorewithme.request.client.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import ru.practicum.explorewithme.request.error.ApiError;
import ru.practicum.explorewithme.request.error.exception.NotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class UserClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    private final ObjectMapper objectMapper;

    public UserClientErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("UserClient methodKey: {}, response: {}", methodKey, response);

        try {
            if (response.status() == 404) {
                ApiError error = parseErrorBody(response);
                return new NotFoundException(error.getMessage());
            }
        } catch (Exception e) {
            log.warn("Сломался в UserClientErrorDecoder, methodKey {}", methodKey, e);
        }

        return defaultDecoder.decode(methodKey, response);
    }

    private ApiError parseErrorBody(Response response) {
        try {
            if (response.body() != null) {
                String body = StreamUtils.copyToString(
                        response.body().asInputStream(),
                        StandardCharsets.UTF_8
                );

                if (!body.trim().isEmpty()) {
                    return objectMapper.readValue(body, ApiError.class);
                }
            }
        } catch (IOException e) {
            log.warn("Сломался в UserClientErrorDecoder на парсинге объекта response {}", response, e);
        }
        return null;
    }
}
