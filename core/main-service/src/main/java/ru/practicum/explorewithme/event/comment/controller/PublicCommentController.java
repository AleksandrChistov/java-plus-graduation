package ru.practicum.explorewithme.event.comment.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.service.PublicCommentService;

import java.util.List;

@RestController
@RequestMapping("/events")
@Validated
@RequiredArgsConstructor
public class PublicCommentController {
    private final PublicCommentService publicCommentService;

    @GetMapping("/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseCommentDto> getCommentsByEventId(
            @Positive @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return publicCommentService.getCommentsByEventId(eventId, from, size);
    }

    @GetMapping("/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseCommentDto> getAllCommentsByEventIds(
            @NotNull @RequestParam("eventIds") List<Long> eventIds,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return publicCommentService.getAllCommentsByEventIds(eventIds, from, size);
    }
}
