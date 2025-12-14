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
import ru.practicum.explorewithme.api.request.enums.RequestStatus;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;
import ru.practicum.explorewithme.event.client.category.CategoryClient;
import ru.practicum.explorewithme.event.client.request.RequestClient;
import ru.practicum.explorewithme.event.client.user.UserClient;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dao.EventSpecifications;
import ru.practicum.explorewithme.event.dto.AdminEventDto;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;
import ru.practicum.explorewithme.api.event.enums.EventState;
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
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final CategoryClient categoryClient;
    private final RequestClient requestClient;
    private final UserClient userClient;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;

    private final UserMapper userMapper;

    @Override
    public EventFullDto update(Long eventId, UpdateEventRequest updateEventRequest) throws RuleViolationException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

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
            if (LocalDateTime.now().plusHours(1).isAfter(updateEventRequest.getEventDate())) {
                throw new BadRequestException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
            event.setEventDate(updateEventRequest.getEventDate());
        }

        if (Objects.equals(updateEventRequest.getStateAction(), StateAction.REJECT_EVENT.name())) {
            if (Objects.equals(event.getState(), EventState.PUBLISHED)) {
                throw new RuleViolationException("Событие нельзя отклонить, если оно опубликовано (PUBLISHED)");
            }
            event.setState(EventState.CANCELED);
        } else if (Objects.equals(updateEventRequest.getStateAction(), StateAction.PUBLISH_EVENT.name())) {
            if (!Objects.equals(event.getState(), EventState.PENDING)) {
                throw new RuleViolationException("Событие должно находиться в статусе PENDING");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        ResponseCategoryDto categoryDto = categoryClient.getById(event.getCategoryId());

        if (updateEventRequest.getCategory() != null) {
            event.setCategoryId(categoryDto.getId());
        }

        eventRepository.save(event);

        log.info("Администратором обновлено событие c ID {}.", event.getId());

        Long confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        UserShortDto userShortDto = userMapper.toUserShortDto(userClient.getUserById(event.getInitiatorId()));

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
    public List<EventFullDto> getAllByParams(AdminEventDto adminEventDto) {
        Pageable pageable = PageRequest.of(
                adminEventDto.getFrom().intValue() / adminEventDto.getSize().intValue(),
                adminEventDto.getSize().intValue()
        );
        List<Event> events = eventRepository.findAll(EventSpecifications.adminSpecification(adminEventDto), pageable).getContent();

        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

        Map<Long, Long> confirmedRequestsMap = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, eventIds);

        Set<Long> categoryIds = events.stream().map(Event::getCategoryId).collect(Collectors.toSet());

        Map<Long, ResponseCategoryDto> categoryDtos = categoryClient.getAllByIds(categoryIds).stream()
                .collect(Collectors.toMap(ResponseCategoryDto::getId, category -> category));

        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());

        Map<Long, UserShortDto> userShortDtos = userClient.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, userMapper::toUserShortDto));

        StatsParams params = StatsUtil.buildStatsParams(
                eventIds.stream()
                        .map(id -> "/events/" + id)
                        .toList(),
                false
        );

        Map<Long, Long> viewsMap = StatsUtil.getViewsMap(statsClient.getStats(params));

        List<EventFullDto> result = events.stream()
                .map(event -> eventMapper.toEventFullDto(
                        event,
                        categoryDtos.get(event.getCategoryId()),
                        userShortDtos.get(event.getInitiatorId()),
                        confirmedRequestsMap.get(event.getId()),
                        viewsMap.get(event.getId())
                    )
                )
                .toList();
        log.info("Администратором получена информация о {} событиях.", result.size());
        return result;
    }
}
