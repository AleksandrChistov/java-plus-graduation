package ru.practicum.stats.analyzer.dal.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.stats.analyzer.dal.model.similarity.Similarity;
import ru.practicum.stats.analyzer.dal.model.similarity.SimilarityId;

public interface SimilarityRepository extends JpaRepository<Similarity, SimilarityId> {
}
