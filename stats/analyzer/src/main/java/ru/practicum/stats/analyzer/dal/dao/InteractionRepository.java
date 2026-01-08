package ru.practicum.stats.analyzer.dal.dao;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.stats.analyzer.dal.model.interaction.Interaction;
import ru.practicum.stats.analyzer.dal.model.interaction.InteractionId;

import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, InteractionId> {

    List<Interaction> findAllById_UserIdOrderByActionDateTimeDesc(Long userId, Limit limit);

    List<Interaction> findAllById_UserId(Long userId);

    @Query("""
    SELECT new ru.practicum.ewm.stats.proto.RecommendedEventProto(
        i.id.eventId,
        SUM(i.rating) as ratingSum
    )
    FROM Interaction i
    WHERE i.id.eventId IN :eventIds
    GROUP BY i.id.eventId
    ORDER BY ratingSum DESC
    """)
    List<RecommendedEventProto> findGroupedRatingsAsDto(@Param("eventIds") List<Long> eventIds);

}
