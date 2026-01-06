package ru.practicum.stats.aggregator.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface EventsSimilarityService {

    List<EventSimilarityAvro> updateState(UserActionAvro action);

}
