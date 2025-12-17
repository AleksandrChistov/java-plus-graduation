package ru.practicum.explorewithme.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.compilation.client.event.EventClient;
import ru.practicum.explorewithme.compilation.dao.CompilationRepository;
import ru.practicum.explorewithme.compilation.dto.CreateCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationDto;
import ru.practicum.explorewithme.compilation.error.exception.NotFoundException;
import ru.practicum.explorewithme.compilation.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilation.model.Compilation;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventClient eventClient;

    /** === Public endpoints accessible to all users. === */

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
                    .map(compilation -> compilationMapper.toCompilationDto(compilation, Collections.emptySet()))
                    .collect(Collectors.toList());
        }

        List<EventShortDto> eventDtos = eventClient.getAllByIds(eventIds);

        if (eventIds.size() != eventDtos.size()) {
            log.error("Event ids and event dtos size mismatch");
        }

        Map<Long, EventShortDto> eventDtoMap = eventDtos.stream()
                .collect(Collectors.toMap(EventShortDto::getId, e -> e));

        return compilations.stream()
                .map(c -> {
                    Set<EventShortDto> compilationEventDtos = c.getEventIds().stream()
                            .map(eventDtoMap::get)
                            .collect(Collectors.toSet());
                    return compilationMapper.toCompilationDto(c, compilationEventDtos);
                })
                .toList();
    }

    @Override
    public ResponseCompilationDto getCompilation(long compId) {
        log.info("Get compilation with id={}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (compilation.getEventIds().isEmpty()) {
            return compilationMapper.toCompilationDto(compilation, Collections.emptySet());
        }

        Set<EventShortDto> eventShortDtos = new HashSet<>(eventClient.getAllByIds(compilation.getEventIds()));

        return compilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    /** === Admin endpoints accessible only for admins. === */

    @Override
    @Transactional
    public ResponseCompilationDto save(CreateCompilationDto requestCompilationDto) {
        log.info("Save compilation {}", requestCompilationDto);
        Compilation newCompilation = compilationMapper.toCompilation(requestCompilationDto);

        if (requestCompilationDto.getEvents() == null || requestCompilationDto.getEvents().isEmpty()) {
            Compilation saved = compilationRepository.save(newCompilation);
            return compilationMapper.toCompilationDto(saved, Collections.emptySet());
        }

        Set<EventShortDto> eventDtos = new HashSet<>(eventClient.getAllByIds(requestCompilationDto.getEvents()));

        Set<Long> eventIds = eventDtos.stream().map(EventShortDto::getId).collect(Collectors.toSet());

        newCompilation.setEventIds(eventIds);

        Compilation saved = compilationRepository.saveAndFlush(newCompilation);

        return compilationMapper.toCompilationDto(saved, eventDtos);
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

        Set<EventShortDto> eventShortDtos = new HashSet<>(eventClient.getAllByIds(updateCompilationDto.getEvents()));

        Set<Long> eventIds = eventShortDtos.stream().map(EventShortDto::getId).collect(Collectors.toSet());

        fromDb.setEventIds(eventIds);

        Compilation updated = compilationRepository.save(fromDb);

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

}
