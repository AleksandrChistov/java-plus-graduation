package ru.practicum.explorewithme.event.service;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsUtil;
import ru.practicum.StatsView;
import ru.practicum.client.StatsClient;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.request.enums.RequestStatus;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;
import ru.practicum.explorewithme.event.client.category.CategoryClient;
import ru.practicum.explorewithme.event.client.request.RequestClient;
import ru.practicum.explorewithme.event.client.user.UserClient;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dao.EventSpecifications;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventParams;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.api.event.enums.EventState;
import ru.practicum.explorewithme.event.error.exception.BadRequestException;
import ru.practicum.explorewithme.event.error.exception.NotFoundException;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.UserMapper;
import ru.practicum.explorewithme.event.model.Event;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final StatsClient statClient;
    private final RequestClient requestClient;
    private final CategoryClient categoryClient;
    private final UserClient userClient;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    @Override
    public List<EventShortDto> getAllByParams(EventParams params, HttpServletRequest request) {
        log.info("Получение событий с параметрами: {}", params.toString());

        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart())) {
            log.error("Ошибка в параметрах диапазона дат: start={}, end={}", params.getRangeStart(), params.getRangeEnd());
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }

        if (params.getRangeStart() == null) params.setRangeStart(LocalDateTime.now());

        Sort sort = params.getEventsSort().getSort();

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);
        List<Event> events = eventRepository.findAll(EventSpecifications.publicSpecification(params), pageable).stream().toList();

        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

        Map<Long, Long> confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, eventIds);

        if (params.getOnlyAvailable()) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit() > confirmedRequests.get(event.getId()))
                    .toList();
        }

        if (events.isEmpty()) {
            log.warn("Нет свободных событий по указанным параметрам {}", params);
            return Collections.emptyList();
        }

        buildStatsDtoAndHit(request);

        StatsParams statsParams = StatsUtil.buildStatsParams(
                eventIds.stream()
                        .map(id -> "/events/" + id)
                        .toList(),
                false
        );

        Map<Long, Long> views = StatsUtil.getViewsMap(statClient.getStats(statsParams));

        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());

        Map<Long, UserShortDto> userShortDtos = userClient.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, userMapper::toUserShortDto));

        Map<Long, ResponseCategoryDto> categoryDtos = categoryClient.getAllByIds(new HashSet<>(params.getCategories())).stream()
                .collect(Collectors.toMap(ResponseCategoryDto::getId, category -> category));

        List<EventShortDto> result = events.stream()
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

        log.info("Метод вернул {} событий.", result.size());

        return result;
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest request) {
        log.debug("Получен запрос на получение события с ID = {}", eventId);
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено."));

        Long confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        buildStatsDtoAndHit(request);

        ResponseCategoryDto categoryDto = categoryClient.getById(event.getCategoryId());

        UserShortDto userShortDto = userMapper.toUserShortDto(userClient.getUserById(event.getInitiatorId()));

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, 0L);
        }

        StatsParams params = StatsUtil.buildStatsParams(
                Collections.singletonList("/events/" + eventId),
                true,
                event.getPublishedOn()
        );

        Long views = statClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();

        EventFullDto dto = eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, views);
        log.debug("Получено событие с ID={}: {}", eventId, dto);
        return dto;
    }

    @Override
    public EventFullDto getByIdAndState(Long eventId, @Nullable EventState state) {
        log.debug("Получен запрос на получение события с ID = {} и state = {}", eventId, state);

        Event event;
        if (state == null) {
            event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Событие c ID = " + eventId + " не найдено"));
        } else {
            event = eventRepository.findByIdAndState(eventId, state)
                    .orElseThrow(() -> new NotFoundException("Событие c ID = " + eventId + " не найдено"));
        }

        Long confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        ResponseCategoryDto categoryDto = categoryClient.getById(event.getCategoryId());

        UserShortDto userDto = userMapper.toUserShortDto(userClient.getUserById(event.getInitiatorId()));

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userDto, confirmedRequests, 0L);
        }

        StatsParams params = StatsUtil.buildStatsParams(
                Collections.singletonList("/events/" + eventId),
                true,
                event.getPublishedOn()
        );

        Long views = statClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();

        EventFullDto dto = eventMapper.toEventFullDto(event, categoryDto, userDto, confirmedRequests, views);
        log.debug("Получено событие с ID={}: {}", eventId, dto);
        return dto;
    }

    @Override
    public List<EventShortDto> getAllByIds(Set<@Positive Long> eventIds) {
        log.debug("Получен запрос на получение событий с IDs = {}", eventIds);
        List<Event> events = eventRepository.findAllById(eventIds);

        Set<Long> dbEventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

        if (dbEventIds.isEmpty()) {
            log.warn("Нет событий по указанным IDs {}", eventIds);
            return Collections.emptyList();
        }

        Map<Long, Long> confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, dbEventIds);

        StatsParams statsParams = StatsUtil.buildStatsParams(
                dbEventIds.stream()
                        .map(id -> "/events/" + id)
                        .toList(),
                false
        );

        Map<Long, Long> views = StatsUtil.getViewsMap(statClient.getStats(statsParams));

        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());

        Map<Long, UserShortDto> userShortDtos = userClient.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, userMapper::toUserShortDto));

        Set<Long> categoryIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());

        Map<Long, ResponseCategoryDto> categoryDtos = categoryClient.getAllByIds(categoryIds).stream()
                .collect(Collectors.toMap(ResponseCategoryDto::getId, category -> category));

        List<EventShortDto> result = events.stream()
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

        log.info("Метод вернул {} событий.", result.size());

        return result;
    }

    private void buildStatsDtoAndHit(HttpServletRequest request) {
        String ip = StatsUtil.getIpAddressOrDefault(request.getRemoteAddr());

        log.debug("Получен IP-адрес: {}", ip);

        StatsDto statsDto = StatsDto.builder()
                .ip(ip)
                .uri(request.getRequestURI())
                .app("explore-with-me-plus")
                .timestamp(LocalDateTime.now())
                .build();

        log.debug("Сохранение статистики = {}", statsDto);
        statClient.hit(statsDto);
        log.info("Статистика сохранена.");
    }

}