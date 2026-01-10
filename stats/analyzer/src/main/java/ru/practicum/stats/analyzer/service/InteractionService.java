package ru.practicum.stats.analyzer.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.dal.model.interaction.Interaction;

public interface InteractionService {

    Interaction save(UserActionAvro action);

}
