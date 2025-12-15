package ru.practicum.explorewithme.request.client.event;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.api.event.enums.EventState;
import ru.practicum.explorewithme.api.event.service.EventServiceApi;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class EventClientFallback implements EventServiceApi {

    @Override
    public EventFullDto getByIdAndState(Long eventId, EventState state) {
        log.warn("Сервис Event недоступен, fallback вернул null для eventId: {} и state: {}", eventId, state);
        return null;
    }

    @Override
    public List<EventShortDto> getAllByIds(Set<@Positive Long> eventIds) {
        log.warn("Сервис Event недоступен, fallback вернул пустой список для eventIds: {}", eventIds);
        return List.of();
    }
}
