package ru.practicum.explorewithme.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.api.request.enums.RequestStatus;
import ru.practicum.explorewithme.api.request.service.AdminRequestServiceApi;
import ru.practicum.explorewithme.request.service.AdminRequestService;

import java.util.Map;
import java.util.Set;

@RestController
@Validated
@RequiredArgsConstructor
public class AdminRequestController implements AdminRequestServiceApi {
    private final AdminRequestService adminRequestService;

    @Override
    public Map<Long, Long> getRequestsCountsByStatusAndEventIds(RequestStatus status, Set<@Positive Long> eventIds) {
        return adminRequestService.getRequestsCountsByStatusAndEventIds(status, eventIds);
    }
}
