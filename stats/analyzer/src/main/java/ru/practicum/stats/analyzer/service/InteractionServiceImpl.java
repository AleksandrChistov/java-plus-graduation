package ru.practicum.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.dal.dao.InteractionRepository;
import ru.practicum.stats.analyzer.dal.model.interaction.Interaction;
import ru.practicum.stats.analyzer.dal.model.interaction.InteractionId;

@Service
@Slf4j
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final InteractionRepository interactionRepository;

    @Override
    public Interaction save(UserActionAvro action) {
        InteractionId id = InteractionId.of(action.getUserId(), action.getEventId());

        Double rating = mapUserActionWeight(action.getActionType());

        Interaction interaction = Interaction.builder()
                .id(id)
                .rating(rating)
                .actionDateTime(action.getTimestamp())
                .build();

        return interactionRepository.save(interaction);
    }

    private static Double mapUserActionWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

}
