package ru.practicum.explorewithme.event.client.category;


import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import ru.practicum.explorewithme.shared.error.ApiError;
import ru.practicum.explorewithme.shared.error.exception.BadRequestException;
import ru.practicum.explorewithme.shared.error.exception.NotFoundException;
import ru.practicum.explorewithme.shared.error.exception.RuleViolationException;
import ru.practicum.explorewithme.shared.error.exception.ServiceUnavailableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CategoryClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    private final ObjectMapper objectMapper;

    public CategoryClientErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("CategoryClient methodKey: {}, response: {}", methodKey, response);

        try {
            if (response.status() == 400) {
                ApiError error = parseErrorBody(response);
                return new BadRequestException(error.getMessage());
            }

            if (response.status() == 404) {
                ApiError error = parseErrorBody(response);
                return new NotFoundException(error.getMessage());
            }

            if (response.status() == 409) {
                ApiError error = parseErrorBody(response);
                return new RuleViolationException(error.getMessage());
            }

            if (response.status() == 503) {
                ApiError error = parseErrorBody(response);
                return new ServiceUnavailableException(error.getMessage());
            }

        } catch (Exception e) {
            log.warn("Сломался в CategoryClientErrorDecoder, methodKey {}", methodKey, e);
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
            log.warn("Сломался в CategoryClientErrorDecoder на парсинге объекта response {}", response, e);
        }
        return null;
    }
}
