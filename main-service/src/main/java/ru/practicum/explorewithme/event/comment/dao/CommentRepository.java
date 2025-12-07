package ru.practicum.explorewithme.event.comment.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.event.comment.enums.Status;
import ru.practicum.explorewithme.event.comment.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByStatus(Status status, Pageable pageable);

    List<Comment> findAllByEvent_Id(long eventId);

    List<Comment> findAllByStatusAndEvent_Id(Status status, long eventId);

    Optional<Comment> findByIdAndEvent_Id(long commentId, long eventId);

    Page<Comment> findByEventIdAndStatus(Long eventId, Status status, Pageable pageable);

    Page<Comment> findByEventIdInAndStatus(List<Long> eventIds, Status status, Pageable pageable);

}
