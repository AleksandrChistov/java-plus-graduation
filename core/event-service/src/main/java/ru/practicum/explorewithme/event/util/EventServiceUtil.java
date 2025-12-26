package ru.practicum.explorewithme.event.util;

import lombok.experimental.UtilityClass;
import ru.practicum.StatsParams;
import ru.practicum.StatsUtil;
import ru.practicum.StatsView;
import ru.practicum.client.StatsClient;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;
import ru.practicum.explorewithme.event.client.category.CategoryClient;
import ru.practicum.explorewithme.event.client.user.UserClient;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.UserMapper;
import ru.practicum.explorewithme.event.model.Event;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class EventServiceUtil {

    public static StatsParams getStatsParams(Event event, boolean unique) {
        return StatsUtil.buildStatsParams(
                Collections.singletonList("/events/" + event.getId()),
                unique,
                event.getPublishedOn()
        );
    }

    public static Long getStatsViews(StatsClient statsClient, Event event, boolean unique) {
        StatsParams params = getStatsParams(event, unique);

        return statsClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();
    }

    public static Map<Long, Long> getStatsViewsMap(StatsClient statsClient, Set<Long> eventIds) {
        StatsParams statsParams = StatsUtil.buildStatsParams(
                eventIds.stream()
                        .map(id -> "/events/" + id)
                        .toList(),
                false
        );

        return StatsUtil.getViewsMap(statsClient.getStats(statsParams));
    }

    public static Map<Long, ResponseCategoryDto> getResponseCategoryDtoMap(CategoryClient categoryClient, Set<Long> categoriesIds) {
        return categoryClient.getAllByIds(categoriesIds).stream()
                .collect(Collectors.toMap(ResponseCategoryDto::getId, Function.identity()));
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
            Map<Long, Long> views,
            EventMapper eventMapper
    ) {
        return events.stream()
                .map(event ->
                        eventMapper.toEventFullDto(
                                event,
                                categoryDtos.get(event.getCategoryId()),
                                userShortDtos.get(event.getInitiatorId()),
                                confirmedRequests.get(event.getId()),
                                views.get(event.getId())
                        )
                )
                .toList();
    }

    public static List<EventShortDto> getEventShortDtos(
            Map<Long, UserShortDto> userShortDtos,
            Map<Long, ResponseCategoryDto> categoryDtos,
            List<Event> events,
            Map<Long, Long> confirmedRequests,
            Map<Long, Long> views,
            EventMapper eventMapper
    ) {
        return events.stream()
                .map(event ->
                        eventMapper.toEventShortDto(
                                event,
                                categoryDtos.get(event.getCategoryId()),
                                userShortDtos.get(event.getInitiatorId()),
                                confirmedRequests.get(event.getId()),
                                views.get(event.getId())
                        )
                )
                .toList();
    }

}
