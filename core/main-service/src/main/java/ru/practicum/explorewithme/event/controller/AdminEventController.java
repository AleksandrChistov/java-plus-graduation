package ru.practicum.explorewithme.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.dto.AdminEventDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.service.AdminEventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@AllArgsConstructor
@Slf4j
@Validated
public class AdminEventController {

    private final AdminEventService adminEventService;

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequest updateEventRequest
    ) {
        log.info("Обновление администратором события с ID {}. Новые данные: {}", eventId, updateEventRequest.toString());
        return adminEventService.update(eventId, updateEventRequest);
    }

    @GetMapping
    public Collection<EventFullDto> getAllByParams(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<State> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
            @RequestParam(defaultValue = "10") @Positive Long size
    ) {
        AdminEventDto params = AdminEventDto.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        log.info("Запрос событий от администратора. Параметры запроса: {}", params.toString());
        return adminEventService.getAllByParams(params);
    }
}
