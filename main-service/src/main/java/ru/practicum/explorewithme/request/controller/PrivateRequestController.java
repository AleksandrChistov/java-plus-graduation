package ru.practicum.explorewithme.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.request.dto.RequestDto;
import ru.practicum.explorewithme.request.dto.RequestStatusUpdate;
import ru.practicum.explorewithme.request.dto.RequestStatusUpdateResult;
import ru.practicum.explorewithme.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@Validated
@RequiredArgsConstructor
public class PrivateRequestController {
    private final RequestService requestService;

    @GetMapping("/requests")
    public List<RequestDto> getUserRequests(@PathVariable Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto addParticipationRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId
    ) {
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId
    ) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<RequestDto> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public RequestStatusUpdateResult updateRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody @Valid RequestStatusUpdate updateRequest
    ) {
        return requestService.updateRequestStatus(userId, eventId, updateRequest);
    }
}
