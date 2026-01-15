package ru.practicum.stats.analyzer.dal.dto;

import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

@RequiredArgsConstructor
public class EventRatingDto {
    private final long eventId;
    private final double ratingSum;

    public RecommendedEventProto toProto() {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(ratingSum)
                .build();
    }
}
