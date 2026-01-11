package ru.practicum.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.dal.dao.InteractionRepository;
import ru.practicum.stats.analyzer.dal.model.interaction.Interaction;
import ru.practicum.stats.analyzer.dal.model.interaction.InteractionId;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class InteractionServiceImpl implements InteractionService {

    private final InteractionRepository interactionRepository;

    @Override
    public void saveIfWeightHigher(UserActionAvro action) {
        InteractionId id = InteractionId.of(action.getUserId(), action.getEventId());

        Optional<Interaction> oldInteractionOpt = interactionRepository.findById(id);

        Double oldWeight = oldInteractionOpt.map(Interaction::getRating).orElse(0.0);
        Double newWeight = mapUserActionWeight(action.getActionType());

        if (newWeight > oldWeight) {
            interactionRepository.save(Interaction.builder()
                    .id(id)
                    .rating(newWeight)
                    .actionDateTime(action.getTimestamp())
                    .build());
        }
    }

    private static Double mapUserActionWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

}
