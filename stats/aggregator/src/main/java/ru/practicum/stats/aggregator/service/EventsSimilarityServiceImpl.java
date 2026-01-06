package ru.practicum.stats.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.*;

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
        double currentWeight = getUserActionsWeight(action.getEventId(), action.getUserId());
        double newWeight = mapNewUserActionWeight(action.getActionType());

        log.info("Current weight: {}, new weight: {}", currentWeight, newWeight);

        // если в результате пользовательского действия максимальный вес для мероприятия не изменился,
        // то и пересчитывать сходство не требуется.
        if (currentWeight != 0.0 && currentWeight >= newWeight) {
            return Collections.emptyList();
        }

        putUserActionsWeight(action, newWeight);

        log.debug("List of user actions weights for event: {}, {}", action.getEventId(), eventsUserActionsWeights);

        return eventsUserActionsWeights.entrySet()
                .stream()
                .filter(e -> e.getValue().containsKey(action.getUserId()) && e.getKey() != action.getEventId())
                .map(e -> {
                    long eventIdA = Math.min(e.getKey(), action.getEventId());
                    long eventIdB = Math.max(e.getKey(), action.getEventId());

                    double similarityScore = currentWeight == 0.0
                            ? calculateSimilarityScore(eventIdA, eventIdB)
                            : recalculateSimilarityScore(action, e, eventIdA, eventIdB, newWeight, currentWeight);

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

    private double calculateSimilarityScore(long eventIdA, long eventIdB) {
        log.info("Calculating similarity score for event A: {} and event B: {}", eventIdA, eventIdB);

        double minWeightSum = eventsUserActionsWeights
                .computeIfAbsent(eventIdA, key -> new HashMap<>())
                .entrySet()
                .stream()
                .map(userAction -> {
                    double weightA = userAction.getValue();
                    double weightB = getUserActionsWeight(eventIdB, userAction.getKey());
                    return Math.min(weightA, weightB);
                })
                .reduce(0.0, Double::sum);

        log.info("Min weight sum for event A: {} and event B: {} = {}", eventIdA, eventIdB, minWeightSum);

        putMinWeightsSum(eventIdA, eventIdB, minWeightSum);

        double weightSumOfEventA = eventsUserActionsWeights
                .computeIfAbsent(eventIdA, key -> new HashMap<>())
                .values()
                .stream().reduce(0.0, Double::sum);

        double weightSumOfEventB = eventsUserActionsWeights
                .computeIfAbsent(eventIdB, key -> new HashMap<>())
                .values()
                .stream().reduce(0.0, Double::sum);

        log.info("Weight sum of event A: {} = {}, weight sum of event B: {} = {}", eventIdA, weightSumOfEventA, eventIdB, weightSumOfEventB);

        putWeightsSum(eventIdA, weightSumOfEventA);
        putWeightsSum(eventIdB, weightSumOfEventB);

        return minWeightSum / (Math.sqrt(weightSumOfEventA) * Math.sqrt(weightSumOfEventB));
    }

    private double recalculateSimilarityScore(UserActionAvro action, Map.Entry<Long, Map<Long, Double>> e, long eventIdA, long eventIdB, double newWeight, double currentWeight) {
        log.info("Recalculating similarity score for event A: {} and event B: {}", eventIdA, eventIdB);

        double oldMinWeightSum = getMinWeightsSum(eventIdA, eventIdB);

        log.info("Old min weight sum for event A: {} and event B: {} = {}", eventIdA, eventIdB, oldMinWeightSum);

        double oldWeightSumOfEventA = getWeightsSum(eventIdA);
        double oldWeightSumOfEventB = getWeightsSum(eventIdB);

        double otherWeight = e.getValue().get(action.getUserId());

        double minNew = Math.min(newWeight, otherWeight);
        double minOld = Math.min(currentWeight, otherWeight);

        double minDiff = minNew - minOld;

        log.info("Min diff for minNew {} and minOld {} = {}", minNew, minOld, minDiff);

        double newMinWeightSum = oldMinWeightSum + minDiff;

        if (minDiff != 0.0) {
            putMinWeightsSum(eventIdA, eventIdB, newMinWeightSum);
        }

        double weightDiff = newWeight - currentWeight;

        log.info("Weight diff for newWeight {} and currentWeight {} = {}", newWeight, currentWeight, weightDiff);

        if (action.getEventId() < e.getKey()) {
            // Вычислим новую сумму весов события A:
            double newWeightSumOfEventA = oldWeightSumOfEventA + weightDiff;

            putWeightsSum(eventIdA, newWeightSumOfEventA);

            // Вычислим новое значение коэффициента сходства мероприятий A и D:
            return newMinWeightSum / (Math.sqrt(newWeightSumOfEventA) * Math.sqrt(oldWeightSumOfEventB));
        } else {
            // Вычислим новую сумму весов события B:
            double newWeightSumOfEventB = oldWeightSumOfEventB + weightDiff;

            putWeightsSum(eventIdB, newWeightSumOfEventB);

            // Вычислим новое значение коэффициента сходства мероприятий A и D:
            return newMinWeightSum / (Math.sqrt(oldWeightSumOfEventA) * Math.sqrt(newWeightSumOfEventB));
        }
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

    public void putMinWeightsSum(long eventA, long eventB, double minWeightSum) {
        eventsPairMinWeightsSum
                .computeIfAbsent(eventA, e -> new HashMap<>())
                .put(eventB, minWeightSum);
    }

    public double getMinWeightsSum(long eventA, long eventB) {
        return eventsPairMinWeightsSum
                .computeIfAbsent(eventA, e -> new HashMap<>())
                .getOrDefault(eventB, 0.0);
    }

    private void putWeightsSum(long eventId, double newWeightSumOfEventA) {
        eventsWeightsSum.put(eventId, newWeightSumOfEventA);
    }

    private Double getWeightsSum(long eventId) {
        return eventsWeightsSum.getOrDefault(eventId, 0.0);
    }

}
