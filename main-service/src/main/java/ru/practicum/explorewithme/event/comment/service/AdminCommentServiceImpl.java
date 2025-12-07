package ru.practicum.explorewithme.event.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.event.comment.dao.CommentRepository;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.event.comment.enums.Status;
import ru.practicum.explorewithme.event.comment.mapper.CommentMapper;
import ru.practicum.explorewithme.event.comment.model.Comment;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.enums.State;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminCommentServiceImpl implements AdminCommentService {

    private final CommentRepository commentRepository;

    private final EventRepository eventRepository;

    private final CommentMapper commentMapper;

    @Override
    public List<ResponseCommentDto> getAll(Status status, int from, int size) {
        log.info("Get all comments with status={} from={} size={}", status, from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());

        Page<Comment> comments;

        if (status == null) {
            comments = commentRepository.findAll(pageable);
        } else {
            comments = commentRepository.findAllByStatus(status, pageable);
        }

        log.debug("Found {} comments", comments.getSize());

        return commentMapper.toResponseCommentDtos(comments.getContent());
    }

    @Override
    public List<ResponseCommentDto> getByEventId(long eventId, Status status) {
        log.info("Get comments by eventId={} with status={}", eventId, status);

        eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Published event with id=" + eventId + " was not found"));

        List<Comment> comments;

        if (status == null) {
            comments = commentRepository.findAllByEvent_Id(eventId);
        } else {
            comments = commentRepository.findAllByStatusAndEvent_Id(status, eventId);
        }

        log.debug("Found {} comments", comments.size());

        return commentMapper.toResponseCommentDtos(comments);
    }

    @Override
    @Transactional
    public void update(long eventId, long commentId, UpdateCommentDto commentDto) {
        log.info("Update comment with id={} and eventId={} with new data={}", commentId, eventId, commentDto);

        Comment comment = commentRepository.findByIdAndEvent_Id(commentId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " and eventId=" + eventId + " was not found"));

        commentMapper.updateCommentStatusFromDto(commentDto, comment);

        commentRepository.save(comment);

        log.debug("Updated comment={}", comment);
    }
}
