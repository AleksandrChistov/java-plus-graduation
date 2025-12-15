package ru.practicum.explorewithme.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsParams;
import ru.practicum.StatsUtil;
import ru.practicum.StatsView;
import ru.practicum.client.StatsClient;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.api.event.enums.EventState;
import ru.practicum.explorewithme.api.request.enums.RequestStatus;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;
import ru.practicum.explorewithme.event.client.category.CategoryClient;
import ru.practicum.explorewithme.event.client.request.RequestClient;
import ru.practicum.explorewithme.event.client.user.UserClient;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;
import ru.practicum.explorewithme.event.enums.StateAction;
import ru.practicum.explorewithme.event.error.exception.BadRequestException;
import ru.practicum.explorewithme.event.error.exception.NotFoundException;
import ru.practicum.explorewithme.event.error.exception.RuleViolationException;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.LocationMapper;
import ru.practicum.explorewithme.event.mapper.UserMapper;
import ru.practicum.explorewithme.event.model.Event;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PrivateEventServiceImpl implements PrivateEventService {

    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final RequestClient requestClient;
    private final EventRepository eventRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;

    private final UserMapper userMapper;

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }

        UserShortDto userShortDto = userMapper.toUserShortDto(userClient.getUserById(userId));

        ResponseCategoryDto categoryDto = categoryClient.getById(newEventDto.getCategory());

        Event newEvent = eventMapper.toEvent(newEventDto, userShortDto.getId(), categoryDto.getId());

        newEvent = eventRepository.saveAndFlush(newEvent);

        log.info("Событие c ID {} создано пользователем с ID {}.", newEvent.getId(), userId);

        return eventMapper.toEventFullDto(newEvent, categoryDto, userShortDto, 0L, 0L);
    }

    @Override
    public EventFullDto update(Long userId, Long eventId, UpdateEventRequest updateEventRequest) {
        UserShortDto userShortDto = userMapper.toUserShortDto(userClient.getUserById(userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!Objects.equals(userShortDto.getId(), event.getInitiatorId())) {
            log.error("Пользователь с ID {} пытается обновить чужое событие с ID {}", userId, eventId);
            throw new RuleViolationException("Пользователь с ID " + userId + " не является инициатором события c ID " + eventId);
        }

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            log.error("Невозможно обновить событие с ID {}: неверный статус события", eventId);
            throw new RuleViolationException("Изменить можно только события в статусах PENDING и CANCELED");
        }

        if (updateEventRequest.getEventDate() != null &&
                updateEventRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Ошибка в дате события с ID {}: новая дата ранее двух часов от текущего момента", eventId);
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два " +
                    "часа от текущего момента");
        }

        ResponseCategoryDto categoryDto = categoryClient.getById(updateEventRequest.getCategory() != null ? updateEventRequest.getCategory() : event.getCategoryId());

        if (updateEventRequest.getCategory() != null) {
            event.setCategoryId(categoryDto.getId());
        }

        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }

        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }

        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }

        if (updateEventRequest.getLocation() != null) {
            event.setLocation(locationMapper.toEntity(updateEventRequest.getLocation()));
        }

        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }

        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }

        if (updateEventRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventRequest.getRequestModeration());
        }

        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(updateEventRequest.getEventDate());
        }

        if (Objects.equals(updateEventRequest.getStateAction(), StateAction.CANCEL_REVIEW.name())) {
            event.setState(EventState.CANCELED);
        } else if (Objects.equals(updateEventRequest.getStateAction(), StateAction.SEND_TO_REVIEW.name())) {
            event.setState(EventState.PENDING);
        }

        eventRepository.save(event);

        log.info("Событие с ID {} обновлено пользователем с ID {}.", eventId, userId);

        Long confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, 0L);
        }

        StatsParams params = StatsUtil.buildStatsParams(
                Collections.singletonList("/events/" + eventId),
                false,
                event.getPublishedOn()
        );

        Long views = statsClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();

        return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getById(Long userId, Long eventId) {
        UserShortDto userShortDto = userMapper.toUserShortDto(userClient.getUserById(userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!Objects.equals(userShortDto.getId(), event.getInitiatorId())) {
            log.error("Пользователь с ID {} пытается получить чужое событие с ID {}", userId, eventId);
            throw new RuleViolationException("Пользователь с ID " + userId + " не является инициатором события c ID " + eventId);
        }

        Long confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        ResponseCategoryDto categoryDto = categoryClient.getById(event.getCategoryId());

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, 0L);
        }

        StatsParams params = StatsUtil.buildStatsParams(
                Collections.singletonList("/events/" + eventId),
                false,
                event.getPublishedOn()
        );

        Long views = statsClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();

        return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAll(Long userId, int from, int size) {
        log.info("Получение всех событий пользователя с ID: {}, from: {}, size: {}.", userId, from, size);

        UserShortDto userShortDto = userMapper.toUserShortDto(userClient.getUserById(userId));

        if (userShortDto == null) {
            throw new NotFoundException("Пользователь c ID " + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorIdOrderByEventDateDesc(userId, pageable).stream().toList();

        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

        Map<Long, Long> confirmedRequestsMap = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, eventIds);

        Set<Long> categoryIds = events.stream().map(Event::getCategoryId).collect(Collectors.toSet());

        Map<Long, ResponseCategoryDto> categoryDtos = categoryClient.getAllByIds(categoryIds).stream()
                .collect(Collectors.toMap(ResponseCategoryDto::getId, category -> category));

        StatsParams params = StatsUtil.buildStatsParams(
                eventIds.stream()
                        .map(id -> "/events/" + id)
                        .toList(),
                false
        );

        Map<Long, Long> viewsMap = StatsUtil.getViewsMap(statsClient.getStats(params));

        return events.stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        categoryDtos.get(event.getCategoryId()),
                        userShortDto,
                        confirmedRequestsMap.get(event.getId()),
                        viewsMap.get(event.getId())
                    )
                )
                .toList();
    }
}
