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
import ru.practicum.explorewithme.category.dao.CategoryRepository;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.error.exception.BadRequestException;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.error.exception.RuleViolationException;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.enums.StateAction;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.LocationMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.request.dao.RequestRepository;
import ru.practicum.explorewithme.request.enums.Status;
import ru.practicum.explorewithme.user.dao.UserRepository;
import ru.practicum.explorewithme.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PrivateEventServiceImpl implements PrivateEventService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория " + newEventDto.getCategory() + " не найдена."));

        Event newEvent = eventMapper.toEvent(newEventDto, initiator, category);
        eventRepository.save(newEvent);
        log.info("Событие c ID {} создано пользователем с ID {}.", newEvent.getId(), userId);
        return eventMapper.toEventFullDto(newEvent, 0L, 0L);
    }

    @Override
    public EventFullDto update(Long userId, Long eventId, UpdateEventRequest updateEventRequest) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            log.error("Пользователь с ID {} пытается обновить чужое событие с ID {}", userId, eventId);
            throw new RuleViolationException("Пользователь с ID " + userId + " не является инициатором события c ID " + eventId);
        }

        if (event.getState() != State.PENDING && event.getState() != State.CANCELED) {
            log.error("Невозможно обновить событие с ID {}: неверный статус события", eventId);
            throw new RuleViolationException("Изменить можно только события в статусах PENDING и CANCELED");
        }

        if (updateEventRequest.getEventDate() != null &&
                updateEventRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Ошибка в дате события с ID {}: новая дата ранее двух часов от текущего момента", eventId);
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два " +
                    "часа от текущего момента");
        }

        if (updateEventRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
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
            event.setState(State.CANCELED);
        } else if (Objects.equals(updateEventRequest.getStateAction(), StateAction.SEND_TO_REVIEW.name())) {
            event.setState(State.PENDING);
        }

        eventRepository.save(event);

        log.info("Событие с ID {} обновлено пользователем с ID {}.", eventId, userId);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), Status.CONFIRMED);

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, confirmedRequests, 0L);
        }

        StatsParams params = StatsUtil.buildStatsParams(
                Collections.singletonList("/events/" + eventId),
                false,
                event.getPublishedOn()
        );

        Long views = statsClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();

        return eventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getById(Long userId, Long eventId) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            log.error("Пользователь с ID {} пытается получить чужое событие с ID {}", userId, eventId);
            throw new RuleViolationException("Пользователь с ID " + userId + " не является инициатором события c ID " + eventId);
        }
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), Status.CONFIRMED);

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, confirmedRequests, 0L);
        }

        StatsParams params = StatsUtil.buildStatsParams(
                Collections.singletonList("/events/" + eventId),
                false,
                event.getPublishedOn()
        );

        Long views = statsClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();

        return eventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAll(Long userId, int from, int size) {
        log.info("Поиск пользователя с ID {}.", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorIdOrderByEventDateDesc(userId, pageable).stream().toList();

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = StatsUtil.getConfirmedRequestsMap(requestRepository.getConfirmedRequestsByEventIds(eventIds));

        StatsParams params = StatsUtil.buildStatsParams(
                eventIds.stream()
                        .map(id -> "/events/" + id)
                        .toList(),
                false
        );

        Map<Long, Long> viewsMap = StatsUtil.getViewsMap(statsClient.getStats(params));

        return events.stream()
                .map(e -> eventMapper.toEventShortDto(e, confirmedRequestsMap.get(e.getId()), viewsMap.get(e.getId())))
                .toList();
    }
}
