package ru.practicum.explorewithme.api.event.service;

import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;

public interface EventServiceApi {
    String URL = "/users/{userId}/events";

    @GetMapping(path = EventServiceApi.URL + "/{eventId}",produces = MediaType.APPLICATION_JSON_VALUE)
    EventFullDto getById(
            @Positive @PathVariable @Positive Long userId,
            @Positive @PathVariable @Positive Long eventId
    );

}
