package ru.practicum.explorewithme.event.client.request;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explorewithme.api.request.dto.RequestDto;
import ru.practicum.explorewithme.api.request.enums.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RequestClientFallback implements RequestClient {

    @Override
    public Map<Long, Long> getRequestsCountsByStatusAndEventIds(RequestStatus status, Set<@Positive Long> eventIds) {
        log.warn("Сервис Request недоступен, fallback отдал пустую мапу, параметры запроса status: {}, eventIds: {}", status, eventIds);
        return eventIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        e -> 0L
                ));
    }

    @Override
    public List<RequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.warn("Сервис Request недоступен, fallback отдал пустой список, параметры запроса userId: {}, eventId: {}", userId, eventId);
        return List.of();
    }

}
