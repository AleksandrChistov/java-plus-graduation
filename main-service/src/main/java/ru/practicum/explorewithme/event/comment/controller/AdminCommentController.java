package ru.practicum.explorewithme.event.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.event.comment.enums.Status;
import ru.practicum.explorewithme.event.comment.service.AdminCommentService;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = AdminCommentController.URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminCommentController {

    public static final String URL = "/admin/events";

    private final AdminCommentService adminCommentService;

    @GetMapping("/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseCommentDto> getAll(
            @RequestParam(required = false) Status status,
            @PositiveOrZero @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return adminCommentService.getAll(status, from, size);
    }

    @GetMapping("/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseCommentDto> getByEventId(
            @Positive @PathVariable Long eventId,
            @RequestParam(required = false) Status status
    ) {
        return adminCommentService.getByEventId(eventId, status);
    }

    @PatchMapping("/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @Positive @PathVariable Long eventId,
            @Positive @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto commentDto
    ) {
        adminCommentService.update(eventId, commentId, commentDto);
    }
}
