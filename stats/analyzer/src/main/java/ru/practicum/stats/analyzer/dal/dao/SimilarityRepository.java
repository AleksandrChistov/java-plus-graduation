package ru.practicum.stats.analyzer.dal.dao;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.analyzer.dal.model.similarity.Similarity;
import ru.practicum.stats.analyzer.dal.model.similarity.SimilarityId;

import java.util.List;
import java.util.Set;

public interface SimilarityRepository extends JpaRepository<Similarity, SimilarityId> {

    List<Similarity> findAllById_Event1InAndId_Event2NotInOrId_Event1NotInAndId_Event2InOrderBySimilarityDesc(Set<Long> eventIds, Limit limit);

    @Query("SELECT s FROM Similarity s WHERE s.id.event1 = :eventId AND s.id.event2 IN :eventIds OR s.id.event2 = :eventId AND s.id.event1 IN :eventIds ORDER BY s.similarity DESC LIMIT :limit")
    List<Similarity> findAllByEvent1EqualsPredictedAndEvent2InInteractedOrderBySimilarityDesc(@Param("predictedEventId") Long eventId, @Param("interactedEventIds") Set<Long> eventIds, @Param("limit") int limit);

    @Query("SELECT s FROM Similarity s WHERE s.id.event1 = :eventId OR s.id.event2 = :eventId AND s.id.event1 NOT IN :eventIds AND s.id.event2 NOT IN :eventIds ORDER BY s.similarity DESC LIMIT :limit")
    List<Similarity> findAllByEventIdAndNotInInteractedOrderBySimilarityDesc(@Param("eventId") Long eventId, @Param("eventIds") Set<Long> interactedEventIds, @Param("limit") int limit);

}
