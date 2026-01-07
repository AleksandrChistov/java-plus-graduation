package ru.practicum.stats.analyzer.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.dal.model.similarity.Similarity;

public interface EventsSimilarityService {

    Similarity save(EventSimilarityAvro event);

}
