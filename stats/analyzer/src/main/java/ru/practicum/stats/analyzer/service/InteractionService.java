package ru.practicum.stats.analyzer.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface InteractionService {

    void saveIfWeightHigher(UserActionAvro action);

}
