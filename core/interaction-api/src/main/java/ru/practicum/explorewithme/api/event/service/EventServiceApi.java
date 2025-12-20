package ru.practicum.explorewithme.api.event.service;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.api.event.enums.EventState;

import java.util.List;
import java.util.Set;

public interface EventServiceApi {
    String URL = "/events";

    @GetMapping(path = URL + "/{eventId}/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    EventFullDto getByIdAndState(@PathVariable @Positive Long eventId, @RequestParam @Nullable EventState state);

    @GetMapping(path = URL + "/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
    List<EventShortDto> getAllByIds(@RequestParam @NotNull Set<@Positive Long> eventIds);

    @GetMapping(path = URL + "/check-categories", produces = MediaType.APPLICATION_JSON_VALUE)
    boolean isCategoriesLinked(@RequestParam @Nullable Set<@Positive Long> categoryIds);

}
