package ru.practicum.explorewithme.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.user.mapper.UserMapper;
import ru.practicum.explorewithme.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class},
        imports = {State.class, LocalDateTime.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", expression = "java(State.PENDING)")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    @Mapping(target = "category", source = "category")
    Event toEvent(NewEventDto newEventDto, User initiator, Category category);

    @Mapping(target = "confirmedRequests", expression = "java(confirmedRequests != null ? confirmedRequests : 0L)")
    @Mapping(target = "views", expression = "java(views != null ? views : 0L)")
    @Mapping(target = "state", expression = "java(String.valueOf(event.getState()))")
    EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views);

    @Mapping(target = "confirmedRequests", expression = "java(confirmedRequests != null ? confirmedRequests : 0L)")
    @Mapping(target = "views", expression = "java(views != null ? views : 0L)")
    EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views);
}
