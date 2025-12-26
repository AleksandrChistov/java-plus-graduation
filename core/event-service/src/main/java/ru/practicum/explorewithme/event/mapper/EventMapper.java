package ru.practicum.explorewithme.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.api.event.enums.EventState;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;
import ru.practicum.explorewithme.event.enums.StateAction;
import ru.practicum.explorewithme.event.model.Event;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {LocationMapper.class},
        imports = {EventState.class, LocalDateTime.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", expression = "java(EventState.PENDING)")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    Event toEvent(NewEventDto newEventDto, Long initiatorId, Long categoryId);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "confirmedRequests", expression = "java(confirmedRequests != null ? confirmedRequests : 0L)")
    @Mapping(target = "views", expression = "java(views != null ? views : 0L)")
    @Mapping(target = "state", expression = "java(String.valueOf(event.getState()))")
    EventFullDto toEventFullDto(Event event, ResponseCategoryDto category, UserShortDto initiator, Long confirmedRequests, Long views);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "confirmedRequests", expression = "java(confirmedRequests != null ? confirmedRequests : 0L)")
    @Mapping(target = "views", expression = "java(views != null ? views : 0L)")
    EventShortDto toEventShortDto(Event event, ResponseCategoryDto category, UserShortDto initiator, Long confirmedRequests, Long views);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true) // уже была создана
    @Mapping(target = "title", source = "updatedEvent.title")
    @Mapping(target = "annotation", source = "updatedEvent.annotation")
    @Mapping(target = "description", source = "updatedEvent.description")
    @Mapping(target = "location", source = "updatedEvent.location")
    @Mapping(target = "paid", source = "updatedEvent.paid")
    @Mapping(target = "participantLimit", source = "updatedEvent.participantLimit")
    @Mapping(target = "requestModeration", source = "updatedEvent.requestModeration")
    @Mapping(target = "eventDate", source = "updatedEvent.eventDate")
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "state", expression = "java(mapStateAction(updatedEvent.getStateAction()))")
    void updateEvent(@MappingTarget Event event, UpdateEventRequest updatedEvent, Long categoryId);

    default EventState mapStateAction(StateAction stateAction) {
        if (stateAction == null) return null;
        return switch (stateAction) {
            case StateAction.CANCEL_REVIEW, StateAction.REJECT_EVENT -> EventState.CANCELED;
            case StateAction.SEND_TO_REVIEW -> EventState.PENDING;
            case StateAction.PUBLISH_EVENT -> EventState.PUBLISHED;
        };
    }

}
