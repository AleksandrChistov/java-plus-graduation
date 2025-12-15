package ru.practicum.explorewithme.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.comment.client.event.EventClient;
import ru.practicum.explorewithme.comment.dao.CommentRepository;
import ru.practicum.explorewithme.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.comment.enums.Status;
import ru.practicum.explorewithme.comment.error.exception.NotFoundException;
import ru.practicum.explorewithme.comment.mapper.CommentMapper;
import ru.practicum.explorewithme.comment.model.Comment;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicCommentServiceImpl implements PublicCommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EventClient eventClient;

    @Override
    public List<ResponseCommentDto> getCommentsByEventId(Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());

        EventFullDto eventDto = eventClient.getByIdAndState(eventId, null);

        if (eventDto == null) {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }

        Page<Comment> comments = commentRepository.findByEventIdAndStatus(eventId, Status.PUBLISHED, pageable);

        return commentMapper.toResponseCommentDtos(comments.getContent());
    }

    @Override
    public List<ResponseCommentDto> getAllCommentsByEventIds(List<Long> eventIds, int from, int size) {
        if (eventIds == null || eventIds.isEmpty()) {
            throw new IllegalArgumentException("Список eventIds не может быть пустым");
        }
        List<Long> existingEventIds = eventClient.getAllByIds(new HashSet<>(eventIds))
                .stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toList());

        if (existingEventIds.isEmpty()) {
            throw new NotFoundException("Не найдено ни одного события из списка: " + eventIds);
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());

        Page<Comment> comments = commentRepository.findByEventIdInAndStatus(existingEventIds,
                Status.PUBLISHED,
                pageable);

        return commentMapper.toResponseCommentDtos(comments.getContent());
    }

}
