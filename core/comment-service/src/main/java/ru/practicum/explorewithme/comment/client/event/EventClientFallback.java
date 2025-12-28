package ru.practicum.explorewithme.comment.client.event;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.api.event.enums.EventState;
import ru.practicum.explorewithme.comment.error.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class EventClientFallback implements EventClient {

    @Override
    public EventFullDto getByIdAndState(Long eventId, @Nullable EventState state) {
        log.warn("Сервис Event недоступен, fallback кинул ServiceUnavailableException для eventId: {} и state: {}", eventId, state);
        throw new ServiceUnavailableException("Сервис Event недоступен");
    }

    @Override
    public List<EventShortDto> getAllByIds(Set<@Positive Long> eventIds) {
        log.warn("Сервис Event недоступен, fallback вернул пустой список для eventIds: {}", eventIds);
        return List.of();
    }

}
