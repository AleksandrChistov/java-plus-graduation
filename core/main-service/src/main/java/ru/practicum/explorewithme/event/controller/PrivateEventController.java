package ru.practicum.explorewithme.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;
import ru.practicum.explorewithme.event.service.PrivateEventService;

import java.util.Collection;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventController {

    private final PrivateEventService privateEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody NewEventDto newEventDto
    ) {
        log.info("Создание события пользователем с ID {} и данными {}", userId, newEventDto);
        return privateEventService.create(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody UpdateEventRequest updateEventRequest
    ) {
        log.info("Обновление пользователем c ID {} события c ID {}. Новые данные: {}", userId, eventId, updateEventRequest);
        return privateEventService.update(userId, eventId, updateEventRequest);
    }

    @GetMapping
    public Collection<EventShortDto> getAll(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Получение списка событий созданных пользователем с ID {}", userId);
        return privateEventService.getAll(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getById(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId
    ) {
        log.info("Получение пользователем с ID {} информации о событии с ID {}", userId, eventId);
        return privateEventService.getById(userId, eventId);
    }
}
