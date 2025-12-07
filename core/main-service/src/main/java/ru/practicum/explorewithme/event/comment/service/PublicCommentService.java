package ru.practicum.explorewithme.event.comment.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;

import java.util.List;

public interface PublicCommentService {
    List<ResponseCommentDto> getCommentsByEventId(@Positive Long eventId, int from, int size);

    List<ResponseCommentDto> getAllCommentsByEventIds(@NotNull List<Long> eventIds, int from, int size);
}
