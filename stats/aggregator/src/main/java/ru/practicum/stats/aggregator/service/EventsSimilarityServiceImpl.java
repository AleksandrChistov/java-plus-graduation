package ru.practicum.stats.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EventsSimilarityServiceImpl implements EventsSimilarityService {

    /**
     * Map<Event, Map<User, Weight>> матрица весов действий пользователей c мероприятиями
     * (учитывается только действие с максимальным весом)
     */
    private Map<Long, Map<Long, Double>> eventsUserActionsWeights = new HashMap<>();

    /**
     * Map<Event, WeightsSum> общие частные суммы весов каждого из мероприятий
     * (сумма весов действий пользователей)
     */
    private Map<Long, Double> eventsWeightsSum = new HashMap<>();

    /**
     * Map<Event, Map<Event, S_min>> сумма минимальных весов для каждой пары мероприятий
     */
    private Map<Long, Map<Long, Double>> eventsPairMinWeightsSum = new HashMap<>();


    /**
     * Вычисление сходства мероприятий на основе пользовательского действия
     * @param action действие пользователя над мероприятием
     * @return список коэффициентов сходства мероприятий с тем, над которым произошло действие
     */
    @Override
    public List<EventSimilarityAvro> updateState(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();

        double oldWeight = getUserActionsWeight(action.getEventId(), action.getUserId());
        double newWeight = mapNewUserActionWeight(action.getActionType());

        if (oldWeight >= newWeight) {
            return Collections.emptyList();
        }

        putUserActionsWeight(action, newWeight);

        double oldSum = getWeightsSum(eventId);
        double newSum = oldSum + oldWeight - newWeight;
        putWeightsSum(eventId, newSum);

        return eventsUserActionsWeights.entrySet()
                .stream()
                .filter(e -> e.getValue().containsKey(action.getUserId()) && e.getKey() != action.getEventId())
                .map(e -> {
                    long eventIdA = Math.min(e.getKey(), action.getEventId());
                    long eventIdB = Math.max(e.getKey(), action.getEventId());

                    double newSumMinWeight = updateMinWeightSum(eventId, e.getKey(), userId, oldWeight, newWeight);
                    double similarityScore = calcSimilarity(eventId, e.getKey(), newSumMinWeight);

                    log.info("Similarity of event A: {} and event B: {} = {}", eventIdA, eventIdB, similarityScore);

                    return EventSimilarityAvro.newBuilder()
                            .setEventA(eventIdA)
                            .setEventB(eventIdB)
                            .setScore(similarityScore)
                            .setTimestamp(action.getTimestamp())
                            .build();
                })
                .toList();
    }

    private double calcSimilarity(long eventId, long otherEventId, double newSumMinPairWeight) {
        if (newSumMinPairWeight == 0) return 0;

        double sumEventWeight = getWeightsSum(eventId);
        double sumOtherEventWeight = getWeightsSum(otherEventId);

        return newSumMinPairWeight / (Math.sqrt(sumEventWeight) * Math.sqrt(sumOtherEventWeight));
    }

    private double updateMinWeightSum(long eventId, long otherEventId, long userId, double oldWeight, double newWeight) {
        double oldWeightOtherEvent = getUserActionsWeight(otherEventId, userId);

        double oldMinPairWeight = Math.min(oldWeight, oldWeightOtherEvent);
        double newMinPairWeight = Math.min(newWeight, oldWeightOtherEvent);

        long firstEventId = Math.min(eventId, otherEventId);
        long secondEventId = Math.max(eventId, otherEventId);

        double oldSumMinPairWeight = getMinWeightsSum(firstEventId, secondEventId);

        if (oldMinPairWeight == newMinPairWeight) return oldSumMinPairWeight;

        double newSumMinPairWeight = oldSumMinPairWeight - oldMinPairWeight + newMinPairWeight;

        putMinWeightsSum(firstEventId, secondEventId, newSumMinPairWeight);

        return newSumMinPairWeight;
    }

    private void putUserActionsWeight(UserActionAvro action, Double weight) {
        eventsUserActionsWeights
                .computeIfAbsent(action.getEventId(), e -> new HashMap<>())
                .put(action.getUserId(), weight);
    }

    private Double getUserActionsWeight(long eventId, long userId) {
        return eventsUserActionsWeights
                .computeIfAbsent(eventId, e -> new HashMap<>())
                .getOrDefault(userId, 0.0);
    }

    private static Double mapNewUserActionWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    public void putMinWeightsSum(long eventA, long eventB, double minSum) {
        eventsPairMinWeightsSum
                .computeIfAbsent(eventA, e -> new HashMap<>())
                .put(eventB, minSum);
    }

    public double getMinWeightsSum(long eventA, long eventB) {
        return eventsPairMinWeightsSum
                .computeIfAbsent(eventA, e -> new HashMap<>())
                .getOrDefault(eventB, 0.0);
    }

    private void putWeightsSum(long eventId, double newSum) {
        eventsWeightsSum.put(eventId, newSum);
    }

    private Double getWeightsSum(long eventId) {
        return eventsWeightsSum.getOrDefault(eventId, 0.0);
    }

}
