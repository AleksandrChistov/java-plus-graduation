package ru.practicum.explorewithme.api.request.service;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explorewithme.api.request.dto.RequestDto;

import java.util.List;

public interface RequestServiceApi {

    @GetMapping(path = "/users/{userId}/events/{eventId}/requests", produces = MediaType.APPLICATION_JSON_VALUE)
    List<RequestDto> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId
    );

}
