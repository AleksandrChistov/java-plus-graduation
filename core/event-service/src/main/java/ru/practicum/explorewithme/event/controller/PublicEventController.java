package ru.practicum.explorewithme.event.controller;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.api.event.enums.EventState;
import ru.practicum.explorewithme.api.event.service.EventServiceApi;
import ru.practicum.explorewithme.event.dto.EventParams;
import ru.practicum.explorewithme.event.enums.EventsSort;
import ru.practicum.explorewithme.event.service.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@Validated
@RequiredArgsConstructor
public class PublicEventController implements EventServiceApi {

    private final PublicEventService publicEventService;

    @GetMapping(path = EventServiceApi.URL)
    public List<EventShortDto> getAllByParams(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") EventsSort eventsSort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        EventParams params = EventParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .eventsSort(eventsSort)
                .from(from)
                .size(size)
                .build();
        return publicEventService.getAllByParams(params, request);
    }

    @GetMapping(path = EventServiceApi.URL + "/{eventId}")
    public EventFullDto getById(
            @PathVariable @Positive Long eventId,
            HttpServletRequest request
    ) {
        return publicEventService.getById(eventId, request);
    }

    @Override
    public EventFullDto getByIdAndState(Long eventId, EventState state) {
        return publicEventService.getByIdAndState(eventId, state);
    }

    @Override
    public List<EventShortDto> getAllByIds(Set<Long> eventIds) {
        return publicEventService.getAllByIds(eventIds);
    }

}
