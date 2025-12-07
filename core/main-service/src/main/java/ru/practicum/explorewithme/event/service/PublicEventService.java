package ru.practicum.explorewithme.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventParams;
import ru.practicum.explorewithme.event.dto.EventShortDto;

import java.util.List;

public interface PublicEventService {

    List<EventShortDto> getAllByParams(EventParams eventParams, HttpServletRequest request);

    EventFullDto getById(Long id, HttpServletRequest request);

}
