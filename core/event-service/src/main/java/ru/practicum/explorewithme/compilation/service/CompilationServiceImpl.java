package ru.practicum.explorewithme.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.RecommendationsClient;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.api.request.enums.RequestStatus;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;
import ru.practicum.explorewithme.category.dao.CategoryRepository;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.compilation.dao.CompilationRepository;
import ru.practicum.explorewithme.compilation.dto.CreateCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationDto;
import ru.practicum.explorewithme.compilation.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilation.model.Compilation;
import ru.practicum.explorewithme.event.client.request.RequestClient;
import ru.practicum.explorewithme.event.client.user.UserClient;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.UserMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.shared.error.exception.NotFoundException;
import ru.practicum.explorewithme.shared.util.CategoryServiceUtil;
import ru.practicum.explorewithme.shared.util.EventServiceUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    private final UserClient userClient;
    private final RequestClient requestClient;
    private final RecommendationsClient recommendationsClient;

    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    /**
     * === Public endpoints accessible to all users. ===
     */

    @Override
    public List<ResponseCompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Get compilations with pinned={} from={} size={}", pinned, from, size);
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

        Set<Long> eventIds = compilations.stream()
                .map(Compilation::getEventIds)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        if (eventIds.isEmpty()) {
            return compilations.stream()
                    .map(compilation -> compilationMapper.toCompilationDto(compilation, Collections.emptyList()))
                    .collect(Collectors.toList());
        }

        List<EventShortDto> allEventDtos = getCompilationEventDtos(eventIds);

        Map<Long, EventShortDto> eventDtoMap = allEventDtos.stream()
                .collect(Collectors.toMap(
                        EventShortDto::getId,
                        Function.identity()
                ));

        Map<Long, List<EventShortDto>> compilationEventsMap = compilations.stream()
                .collect(Collectors.toMap(
                        Compilation::getId,
                        c -> c.getEventIds().stream()
                                .map(eventDtoMap::get)
                                .collect(Collectors.toList())
                ));

        return compilations.stream()
                .map(c -> {
                    List<EventShortDto> eventDtos = compilationEventsMap.getOrDefault(c.getId(), Collections.emptyList());

                    return compilationMapper.toCompilationDto(c, eventDtos);
                })
                .toList();
    }

    @Override
    public ResponseCompilationDto getCompilation(long compId) {
        log.info("Get compilation with id={}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (compilation.getEventIds().isEmpty()) {
            return compilationMapper.toCompilationDto(compilation, Collections.emptyList());
        }

        List<EventShortDto> eventShortDtos = getCompilationEventDtos(compilation.getEventIds());

        return compilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    /**
     * === Admin endpoints accessible only for admins. ===
     */

    @Override
    @Transactional
    public ResponseCompilationDto save(CreateCompilationDto requestCompilationDto) {
        log.info("Save compilation {}", requestCompilationDto);

        Compilation newCompilation = compilationMapper.toCompilation(requestCompilationDto);

        if (requestCompilationDto.getEvents() == null || requestCompilationDto.getEvents().isEmpty()) {
            Compilation saved = compilationRepository.save(newCompilation);
            return compilationMapper.toCompilationDto(saved, Collections.emptyList());
        }

        newCompilation.setEventIds(requestCompilationDto.getEvents());

        Compilation saved = compilationRepository.saveAndFlush(newCompilation);

        List<EventShortDto> eventShortDtos = getCompilationEventDtos(requestCompilationDto.getEvents());

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
            return compilationMapper.toCompilationDto(updated, Collections.emptyList());
        }

        fromDb.setEventIds(updateCompilationDto.getEvents());

        Compilation updated = compilationRepository.save(fromDb);

        List<EventShortDto> eventShortDtos = getCompilationEventDtos(updateCompilationDto.getEvents());

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

    private List<EventShortDto> getCompilationEventDtos(Set<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllById(new ArrayList<>(eventIds));

        Map<Long, Long> confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, eventIds);

        Map<Long, Double> ratings = EventServiceUtil.getRatingsMap(recommendationsClient, eventIds);

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = events.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> userShortDtos = EventServiceUtil.getUserShortDtoMap(userClient, userIds, userMapper);

        Map<Long, ResponseCategoryDto> categoryDtos = CategoryServiceUtil.getResponseCategoryDtoMap(categoryRepository, categoryMapper, categoryIds);

        return EventServiceUtil.getEventShortDtos(userShortDtos, categoryDtos, events, confirmedRequests, ratings, eventMapper);
    }

}
