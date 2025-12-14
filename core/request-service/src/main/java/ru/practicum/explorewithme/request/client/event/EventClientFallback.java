package ru.practicum.explorewithme.request.client.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.service.EventServiceApi;

@Slf4j
@Component
public class EventClientFallback implements EventServiceApi {

    @Override
    public EventFullDto getById(Long userId, Long eventId) {
        log.warn("Сервис Event недоступен, fallback вернул null для userId: {} и eventId: {}", userId, eventId);
        return null;
    }
}
