package ru.practicum.explorewithme.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.request.enums.Status;
import ru.practicum.explorewithme.request.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    long countByEventIdAndStatus(Long eventId, Status status);

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);

    Optional<Request> findByIdAndRequesterId(Long id, Long requesterId);

    List<Request> findByEventIdAndStatus(Long eventId, Status status);

    Optional<Request> findByIdAndEventId(Long id, Long eventId);

    @Query("SELECT r.event.id, COUNT(r) " +
            "FROM Request r " +
            "WHERE r.event.id IN :eventIds AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event.id")
    List<Object[]> getConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);
}
