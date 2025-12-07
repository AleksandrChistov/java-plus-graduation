package ru.practicum.explorewithme.event.service;

import jakarta.servlet.http.HttpServletRequest;
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
import ru.practicum.explorewithme.error.exception.BadRequestException;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dao.EventSpecifications;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventParams;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.request.dao.RequestRepository;
import ru.practicum.explorewithme.request.enums.Status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final StatsClient statClient;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> getAllByParams(EventParams params, HttpServletRequest request) {

        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart())) {
            log.error("Ошибка в параметрах диапазона дат: start={}, end={}", params.getRangeStart(), params.getRangeEnd());
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }

        if (params.getRangeStart() == null) params.setRangeStart(LocalDateTime.now());

        Sort sort = params.getEventsSort().getSort();

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);
        List<Event> events = eventRepository.findAll(EventSpecifications.publicSpecification(params), pageable).stream().toList();

        if (events.isEmpty()) {
            log.warn("Нет событий по указанным параметрам {}", params);
            return Collections.emptyList();
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> confirmedRequests = StatsUtil.getConfirmedRequestsMap(requestRepository.getConfirmedRequestsByEventIds(eventIds));

        buildStatsDtoAndHit(request);

        StatsParams statsParams = StatsUtil.buildStatsParams(
                eventIds.stream()
                        .map(id -> "/events/" + id)
                        .toList(),
                false
        );

        Map<Long, Long> views = StatsUtil.getViewsMap(statClient.getStats(statsParams));

        List<EventShortDto> result = events.stream()
                .map(event -> eventMapper.toEventShortDto(event,
                        Optional.ofNullable(confirmedRequests.get(event.getId())).orElse(0L),
                        Optional.ofNullable(views.get(event.getId())).orElse(0L)))
                .toList();
        log.info("Метод вернул {} событий.", result.size());
        return result;
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено."));

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);

        buildStatsDtoAndHit(request);

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, confirmedRequests, 0L);
        }

        StatsParams params = StatsUtil.buildStatsParams(
                Collections.singletonList("/events/" + eventId),
                true,
                event.getPublishedOn()
        );

        Long views = statClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();

        EventFullDto dto = eventMapper.toEventFullDto(event, confirmedRequests, views);
        log.debug("Получено событие с ID={}: {}", eventId, dto);
        return dto;
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