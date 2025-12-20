package ru.practicum.explorewithme.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.api.event.enums.EventState;
import ru.practicum.explorewithme.comment.client.event.EventClient;
import ru.practicum.explorewithme.comment.dao.CommentRepository;
import ru.practicum.explorewithme.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.comment.enums.Status;
import ru.practicum.explorewithme.comment.error.exception.NotFoundException;
import ru.practicum.explorewithme.comment.mapper.CommentMapper;
import ru.practicum.explorewithme.comment.model.Comment;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminCommentServiceImpl implements AdminCommentService {

    private final CommentRepository commentRepository;

    private final EventClient eventClient;

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

        try {
            eventClient.getByIdAndState(eventId, EventState.PUBLISHED);
        } catch (NotFoundException e) {
            throw new NotFoundException("Published event with id=" + eventId + " was not found");
        }

        List<Comment> comments;

        if (status == null) {
            comments = commentRepository.findAllByEventId(eventId);
        } else {
            comments = commentRepository.findAllByStatusAndEventId(status, eventId);
        }

        log.debug("Found {} comments", comments.size());

        return commentMapper.toResponseCommentDtos(comments);
    }

    @Override
    @Transactional
    public void update(long eventId, long commentId, UpdateCommentDto commentDto) {
        log.info("Update comment with id={} and eventId={} with new data={}", commentId, eventId, commentDto);

        Comment comment = commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " and eventId=" + eventId + " was not found"));

        commentMapper.updateCommentStatusFromDto(commentDto, comment);

        commentRepository.save(comment);

        log.debug("Updated comment={}", comment);
    }
}
