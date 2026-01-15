package ru.practicum.explorewithme.shared.util;

import lombok.experimental.UtilityClass;
import ru.practicum.client.RecommendationsClient;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;
import ru.practicum.explorewithme.event.client.user.UserClient;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.UserMapper;
import ru.practicum.explorewithme.event.model.Event;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class EventServiceUtil {

    public static final int MAX_RESULTS = 5;

    public static Map<Long, Double> getRatingsMap(RecommendationsClient recommendationsClient, Set<Long> eventIds) {
        return recommendationsClient.getInteractionsCount(eventIds, MAX_RESULTS)
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }

    public static Map<Long, Double> getRecommendationsMap(RecommendationsClient recommendationsClient, long userId) {
        return recommendationsClient.getRecommendationsForUser(userId, MAX_RESULTS)
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }

    public static Map<Long, UserShortDto> getUserShortDtoMap(UserClient userClient, Set<Long> userIds, UserMapper userMapper) {
        return userClient.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, userMapper::toUserShortDto));
    }

    public static List<EventFullDto> getEventFullDtos(
            Map<Long, UserShortDto> userShortDtos,
            Map<Long, ResponseCategoryDto> categoryDtos,
            List<Event> events,
            Map<Long, Long> confirmedRequests,
            Map<Long, Double> ratings,
            EventMapper eventMapper
    ) {
        return events.stream()
                .map(event ->
                        eventMapper.toEventFullDto(
                                event,
                                categoryDtos.get(event.getCategoryId()),
                                userShortDtos.get(event.getInitiatorId()),
                                confirmedRequests.getOrDefault(event.getId(), 0L),
                                ratings.getOrDefault(event.getId(), 0.0)
                        )
                )
                .toList();
    }

    public static List<EventShortDto> getEventShortDtos(
            Map<Long, UserShortDto> userShortDtos,
            Map<Long, ResponseCategoryDto> categoryDtos,
            List<Event> events,
            Map<Long, Long> confirmedRequests,
            Map<Long, Double> ratings,
            EventMapper eventMapper
    ) {
        return events.stream()
                .map(event ->
                        eventMapper.toEventShortDto(
                                event,
                                categoryDtos.get(event.getCategoryId()),
                                userShortDtos.get(event.getInitiatorId()),
                                confirmedRequests.getOrDefault(event.getId(), 0L),
                                ratings.getOrDefault(event.getId(), 0.0)
                        )
                )
                .toList();
    }

}
