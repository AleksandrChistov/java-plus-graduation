package ru.practicum.explorewithme.event.client.request;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.api.request.enums.RequestStatus;
import ru.practicum.explorewithme.api.request.service.AdminRequestServiceApi;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class RequestClientFallback implements AdminRequestServiceApi {
    @Override
    public Map<Long, Long> getRequestsCountsByStatusAndEventIds(RequestStatus status, Set<@Positive Long> eventIds) {
        log.warn("Сервис Request недоступен, fallback отдал пустую мапу, параметры запроса status: {}, eventIds: {}", status, eventIds);
        return Map.of();
    }
}
