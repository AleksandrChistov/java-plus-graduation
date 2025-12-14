package ru.practicum.explorewithme.api.request.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explorewithme.api.request.enums.RequestStatus;

import java.util.Map;
import java.util.Set;

public interface AdminRequestServiceApi {

    @GetMapping(path = "/admin/users/{userId}/events/requests", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<Long, Long> getRequestsCountsByStatusAndEventIds(
            @NotNull @RequestParam RequestStatus status,
            @NotNull @RequestParam Set<@Positive Long> eventIds
    );

}
