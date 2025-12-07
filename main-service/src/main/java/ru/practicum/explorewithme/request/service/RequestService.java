package ru.practicum.explorewithme.request.service;

import jakarta.validation.Valid;
import ru.practicum.explorewithme.request.dto.RequestDto;
import ru.practicum.explorewithme.request.dto.RequestStatusUpdate;
import ru.practicum.explorewithme.request.dto.RequestStatusUpdateResult;

import java.util.List;

public interface RequestService {

    RequestDto createRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getEventRequests(Long userId, Long eventId);

    RequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, @Valid RequestStatusUpdate updateRequest);

    List<RequestDto> getUserRequests(Long userId);
}
