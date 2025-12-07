package ru.practicum.explorewithme.event.comment.service;

import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.event.comment.enums.Status;

import java.util.List;

public interface AdminCommentService {

    List<ResponseCommentDto> getAll(Status status, int from, int size);

    List<ResponseCommentDto> getByEventId(long eventId, Status status);

    void update(long eventId, long commentId, UpdateCommentDto commentDto);

}
