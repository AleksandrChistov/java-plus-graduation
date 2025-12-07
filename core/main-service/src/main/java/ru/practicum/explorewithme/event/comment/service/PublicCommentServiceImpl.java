package ru.practicum.explorewithme.event.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.event.comment.dao.CommentRepository;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.enums.Status;
import ru.practicum.explorewithme.event.comment.mapper.CommentMapper;
import ru.practicum.explorewithme.event.comment.model.Comment;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.model.Event;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicCommentServiceImpl implements PublicCommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EventRepository eventRepository;

    @Override
    public List<ResponseCommentDto> getCommentsByEventId(Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());

        if (!eventRepository.existsById(eventId)) {
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
        List<Long> existingEventIds = eventRepository.findAllById(eventIds)
                .stream()
                .map(Event::getId)
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
