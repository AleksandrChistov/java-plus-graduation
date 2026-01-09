package ru.practicum.explorewithme.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.RecommendationsClient;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.enums.EventState;
import ru.practicum.explorewithme.api.request.enums.RequestStatus;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;
import ru.practicum.explorewithme.category.dao.CategoryRepository;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.event.client.request.RequestClient;
import ru.practicum.explorewithme.event.client.user.UserClient;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dao.EventSpecifications;
import ru.practicum.explorewithme.event.dto.AdminEventDto;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;
import ru.practicum.explorewithme.event.enums.StateAction;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.UserMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.shared.error.exception.BadRequestException;
import ru.practicum.explorewithme.shared.error.exception.NotFoundException;
import ru.practicum.explorewithme.shared.error.exception.RuleViolationException;
import ru.practicum.explorewithme.shared.util.CategoryServiceUtil;
import ru.practicum.explorewithme.shared.util.EventServiceUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    private final UserClient userClient;
    private final RequestClient requestClient;
    private final RecommendationsClient recommendationsClient;

    private final EventMapper eventMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public EventFullDto update(Long eventId, UpdateEventRequest updateEventRequest) throws RuleViolationException {
        log.info("Администратором обновляется событие c ID {}: {}", eventId, updateEventRequest);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        validateCriticalRules(updateEventRequest, event);

        ResponseCategoryDto categoryDto = CategoryServiceUtil
                .getResponseCategoryDto(categoryRepository, categoryMapper, event.getCategoryId());

        eventMapper.updateEvent(event, updateEventRequest);

        if (Objects.equals(updateEventRequest.getStateAction(), StateAction.PUBLISH_EVENT)) {
            event.setPublishedOn(LocalDateTime.now());
        }

        eventRepository.save(event);

        Long confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        UserShortDto userShortDto = userMapper.toUserShortDto(userClient.getUserById(event.getInitiatorId()));

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, 0.0);
        }

        double rating = EventServiceUtil.getRatingsMap(recommendationsClient, Set.of(event.getId()))
                .getOrDefault(event.getId(), 0.0);

        log.info("Администратором обновлено событие c ID {}.", event.getId());

        return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, rating);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllByParams(AdminEventDto adminEventDto) {
        log.info("Получение администратором событий по параметрам: {}", adminEventDto);

        List<Event> events = eventRepository.findAll(
                EventSpecifications.adminSpecification(adminEventDto),
                makePageable(adminEventDto)
        ).getContent();

        Set<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        Map<Long, Long> confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, eventIds);

        Map<Long, Double> ratings = EventServiceUtil.getRatingsMap(recommendationsClient, eventIds);

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Set<Long> categoriesIds = events.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> userShortDtos = EventServiceUtil.getUserShortDtoMap(userClient, userIds, userMapper);

        Map<Long, ResponseCategoryDto> categoryDtos = CategoryServiceUtil
                .getResponseCategoryDtoMap(categoryRepository, categoryMapper, categoriesIds);

        return EventServiceUtil.getEventFullDtos(userShortDtos, categoryDtos, events, confirmedRequests, ratings, eventMapper);
    }

    private static Pageable makePageable(AdminEventDto adminEventDto) {
        return PageRequest.of(
                adminEventDto.getFrom().intValue() / adminEventDto.getSize().intValue(),
                adminEventDto.getSize().intValue()
        );
    }

    private static void validateCriticalRules(UpdateEventRequest updateEventRequest, Event event) {
        if (updateEventRequest.getEventDate() != null) {
            if (LocalDateTime.now().plusHours(1).isAfter(updateEventRequest.getEventDate())) {
                throw new BadRequestException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
        }

        if (Objects.equals(updateEventRequest.getStateAction(), StateAction.REJECT_EVENT)) {
            if (Objects.equals(event.getState(), EventState.PUBLISHED)) {
                throw new RuleViolationException("Событие нельзя отклонить, если оно опубликовано (PUBLISHED)");
            }
        } else if (Objects.equals(updateEventRequest.getStateAction(), StateAction.PUBLISH_EVENT)) {
            if (!Objects.equals(event.getState(), EventState.PENDING)) {
                throw new RuleViolationException("Событие должно находиться в статусе PENDING");
            }
        }
    }

}
