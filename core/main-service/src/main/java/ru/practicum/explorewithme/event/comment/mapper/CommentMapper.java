package ru.practicum.explorewithme.event.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explorewithme.event.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.event.comment.enums.Status;
import ru.practicum.explorewithme.event.comment.model.Comment;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.user.model.User;

import java.util.List;
import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {LocalDateTime.class, Status.class}
)
public interface CommentMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "authorId", source = "author.id")
    ResponseCommentDto toResponseCommentDto(Comment comment);

    List<ResponseCommentDto> toResponseCommentDtos(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", expression = "java(Status.PENDING)")
    @Mapping(target = "created", expression = "java(LocalDateTime.now())")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "author", source = "author")
    Comment toComment(NewCommentDto newCommentDto, Event event, User author);

    @Mapping(target = "updated", expression = "java(LocalDateTime.now())")
    void updateCommentStatusFromDto(UpdateCommentDto commentDto, @MappingTarget Comment comment);

    @Mapping(target = "updated", expression = "java(LocalDateTime.now())")
    void updateCommentTextFromDto(NewCommentDto commentDto, @MappingTarget Comment comment);

}
