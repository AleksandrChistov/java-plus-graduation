package ru.practicum.explorewithme.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsParams;
import ru.practicum.StatsUtil;
import ru.practicum.client.StatsClient;
import ru.practicum.explorewithme.compilation.dao.CompilationRepository;
import ru.practicum.explorewithme.compilation.dto.CreateCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationDto;
import ru.practicum.explorewithme.compilation.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilation.model.Compilation;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.request.dao.RequestRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;
    private final StatsClient statClient;

    /** === Public endpoints accessible to all users. === */

    @Override
    public List<ResponseCompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository
                    .findAll(pageable)
                    .toList();
        } else {
            compilations = compilationRepository
                    .findAllByPinned(pinned, pageable)
                    .toList();
        }

        List<Event> events = compilations.stream()
                .map(Compilation::getEvents)
                .flatMap(Set::stream)
                .toList();

        if (events.isEmpty()) {
            return compilations.stream()
                    .map(compilation -> compilationMapper.toCompilationDto(compilation, Collections.emptySet()))
                    .collect(Collectors.toList());
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        return compilations.stream()
                .map(c -> {
                    Set<EventShortDto> compilationEventDtos = getEventShortDtos(
                            c.getEvents(),
                            getConfirmedRequests(eventIds),
                            getViews(eventIds)
                    );
                    return compilationMapper.toCompilationDto(c, compilationEventDtos);
                })
                .toList();
    }

    @Override
    public ResponseCompilationDto getCompilation(long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (compilation.getEvents().isEmpty()) {
            return compilationMapper.toCompilationDto(compilation, Collections.emptySet());
        }

        List<Long> eventIds = compilation.getEvents().stream().map(Event::getId).toList();

        Set<EventShortDto> eventShortDtos = getEventShortDtos(
                compilation.getEvents(),
                getConfirmedRequests(eventIds),
                getViews(eventIds)
        );

        return compilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    /** === Admin endpoints accessible only for admins. === */

    @Override
    @Transactional
    public ResponseCompilationDto save(CreateCompilationDto requestCompilationDto) {
        Compilation newCompilation = compilationMapper.toCompilation(requestCompilationDto);

        if (requestCompilationDto.getEvents() == null || requestCompilationDto.getEvents().isEmpty()) {
            Compilation saved = compilationRepository.save(newCompilation);
            return compilationMapper.toCompilationDto(saved, Collections.emptySet());
        }

        Set<Event> events = eventRepository.findAllByIdIn(requestCompilationDto.getEvents());

        newCompilation.setEvents(events);

        Compilation saved = compilationRepository.save(newCompilation);

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Set<EventShortDto> eventShortDtos = getEventShortDtos(
                events,
                getConfirmedRequests(eventIds),
                getViews(eventIds)
        );

        return compilationMapper.toCompilationDto(saved, eventShortDtos);
    }

    @Override
    @Transactional
    public ResponseCompilationDto update(long compId, UpdateCompilationDto updateCompilationDto) {
        Compilation fromDb = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        compilationMapper.updateCompilationFromDto(updateCompilationDto, fromDb);

        if (updateCompilationDto.getEvents() == null || updateCompilationDto.getEvents().isEmpty()) {
            Compilation updated = compilationRepository.save(fromDb);
            return compilationMapper.toCompilationDto(updated, Collections.emptySet());
        }

        Set<Event> events = eventRepository.findAllByIdIn(updateCompilationDto.getEvents());

        fromDb.setEvents(events);

        Compilation updated = compilationRepository.save(fromDb);

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Set<EventShortDto> eventShortDtos = getEventShortDtos(
                events,
                getConfirmedRequests(eventIds),
                getViews(eventIds)
        );

        return compilationMapper.toCompilationDto(updated, eventShortDtos);
    }

    @Override
    @Transactional
    public void delete(long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }

        compilationRepository.deleteById(compId);
    }

    /** === Private internal methods === */

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        return StatsUtil.getConfirmedRequestsMap(requestRepository.getConfirmedRequestsByEventIds(eventIds));
    }

    private Map<Long, Long> getViews(List<Long> eventIds) {
        StatsParams statsParams = StatsUtil.buildStatsParams(
                eventIds.stream()
                        .map(id -> "/events/" + id)
                        .toList(),
                false
        );

        return StatsUtil.getViewsMap(statClient.getStats(statsParams));
    }

    private Set<EventShortDto> getEventShortDtos(Set<Event> events, Map<Long, Long> confirmedRequests, Map<Long, Long> views) {
        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event,
                        Optional.ofNullable(confirmedRequests.get(event.getId())).orElse(0L),
                        Optional.ofNullable(views.get(event.getId())).orElse(0L)))
                .collect(Collectors.toSet());
    }

}
