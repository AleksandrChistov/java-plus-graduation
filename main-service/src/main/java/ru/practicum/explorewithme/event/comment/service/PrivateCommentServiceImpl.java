package ru.practicum.explorewithme.event.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.error.exception.RuleViolationException;
import ru.practicum.explorewithme.event.comment.dao.CommentRepository;
import ru.practicum.explorewithme.event.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.enums.Status;
import ru.practicum.explorewithme.event.comment.mapper.CommentMapper;
import ru.practicum.explorewithme.event.comment.model.Comment;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.user.dao.UserRepository;
import ru.practicum.explorewithme.user.model.User;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrivateCommentServiceImpl implements PrivateCommentService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public ResponseCommentDto create(Long userId, Long eventId, NewCommentDto dto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!event.getState().equals(State.PUBLISHED)) {
            log.error("Ожидается статус события - PUBLISHED, текущий статус: {}", event.getState());
            throw new RuleViolationException("Неопубликованное событие нельзя комментировать.");
        }

        Comment comment = commentMapper.toComment(dto, event, author);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID " + commentId + " не найден."));

        if (!comment.getAuthor().equals(user)) {
            log.error("Пользователь с ID {} не является автором комментария с ID {}", userId, commentId);
            throw new RuleViolationException("Комментарий можен быть изменен/удален только его автором.");
        }

        if (!comment.getEvent().equals(event)) {
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
