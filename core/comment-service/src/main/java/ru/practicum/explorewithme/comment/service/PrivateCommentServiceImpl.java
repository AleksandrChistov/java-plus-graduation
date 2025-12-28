package ru.practicum.explorewithme.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.enums.EventState;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.comment.client.event.EventClient;
import ru.practicum.explorewithme.comment.client.user.UserClient;
import ru.practicum.explorewithme.comment.dao.CommentRepository;
import ru.practicum.explorewithme.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.comment.enums.Status;
import ru.practicum.explorewithme.comment.error.exception.NotFoundException;
import ru.practicum.explorewithme.comment.error.exception.RuleViolationException;
import ru.practicum.explorewithme.comment.mapper.CommentMapper;
import ru.practicum.explorewithme.comment.model.Comment;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrivateCommentServiceImpl implements PrivateCommentService {
    private final UserClient userClient;
    private final EventClient eventClient;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public ResponseCommentDto create(Long userId, Long eventId, NewCommentDto dto) {
        UserDto userDto = userClient.getUserById(userId);
        EventFullDto eventDto = eventClient.getByIdAndState(eventId, null);

        if (!eventDto.getState().equals(EventState.PUBLISHED.name())) {
            log.error("Ожидается статус события - PUBLISHED, текущий статус: {}", eventDto.getState());
            throw new RuleViolationException("Неопубликованное событие нельзя комментировать.");
        }

        Comment comment = commentMapper.toComment(dto, eventDto, userDto);
        commentRepository.save(comment);

        log.info("Новый комментарий создан: {}", comment);
        return commentMapper.toResponseCommentDto(comment);
    }

    public ResponseCommentDto patch(Long userId, Long eventId, Long commentId, NewCommentDto dto) {
        Comment comment = validateComment(userId, eventId, commentId);
        commentMapper.updateCommentTextFromDto(dto, comment);
        log.info("Комментарий с ID {} изменен.", commentId);
        return commentMapper.toResponseCommentDto(comment);
    }

    public void delete(Long userId, Long eventId, Long commentId) {
        validateComment(userId, eventId, commentId);
        commentRepository.deleteById(commentId);
        log.info("Комментарий с ID {} успешно удален.", commentId);
    }

    private Comment validateComment(Long userId, Long eventId, Long commentId) {
        userClient.getUserById(userId);
        eventClient.getByIdAndState(eventId, null);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID " + commentId + " не найден."));

        if (!comment.getAuthorId().equals(userId)) {
            log.error("Пользователь с ID {} не является автором комментария с ID {}", userId, commentId);
            throw new RuleViolationException("Комментарий можен быть изменен/удален только его автором.");
        }

        if (!comment.getEventId().equals(eventId)) {
            log.error("Комментарий с ID {} не относится к событию с ID {}", commentId, eventId);
            throw new RuleViolationException("Комментарий должен соответствовать указанному событию.");
        }

        if (!comment.getStatus().equals(Status.PENDING)) {
            log.error("Ожидается статус коммента - PENDING, текущий статус: {}", comment.getStatus());
            throw new RuleViolationException("Комментарий не доступен для редактирования/удаления после публикации или " +
                    "отклонения администратором.");
        }

        return comment;
    }
}
