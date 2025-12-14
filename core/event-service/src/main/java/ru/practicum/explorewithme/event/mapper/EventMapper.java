package ru.practicum.explorewithme.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.model.Event;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        uses = {LocationMapper.class},
        imports = {State.class, LocalDateTime.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", expression = "java(State.PENDING)")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    Event toEvent(NewEventDto newEventDto, Long initiatorId, Long categoryId);

    @Mapping(target = "confirmedRequests", expression = "java(confirmedRequests != null ? confirmedRequests : 0L)")
    @Mapping(target = "views", expression = "java(views != null ? views : 0L)")
    @Mapping(target = "state", expression = "java(String.valueOf(event.getState()))")
    EventFullDto toEventFullDto(Event event, ResponseCategoryDto category, UserShortDto initiator, Long confirmedRequests, Long views);

    @Mapping(target = "confirmedRequests", expression = "java(confirmedRequests != null ? confirmedRequests : 0L)")
    @Mapping(target = "views", expression = "java(views != null ? views : 0L)")
    EventShortDto toEventShortDto(Event event, ResponseCategoryDto category, UserShortDto initiator, Long confirmedRequests, Long views);
}
