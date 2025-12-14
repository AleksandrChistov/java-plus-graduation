package ru.practicum.explorewithme.event.service;

import ru.practicum.explorewithme.event.dto.AdminEventDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;

import java.util.List;

public interface AdminEventService {

    EventFullDto update(Long eventId, UpdateEventRequest updateEventRequest);

    List<EventFullDto> getAllByParams(AdminEventDto adminEventDto);
}
