package ru.practicum.explorewithme.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.comment.enums.Status;
import ru.practicum.explorewithme.comment.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {LocalDateTime.class, Status.class}
)
public interface CommentMapper {

    @Mapping(target = "eventId", source = "comment.eventId")
    @Mapping(target = "authorId", source = "comment.authorId")
    ResponseCommentDto toResponseCommentDto(Comment comment);

    List<ResponseCommentDto> toResponseCommentDtos(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", expression = "java(Status.PENDING)")
    @Mapping(target = "created", expression = "java(LocalDateTime.now())")
    @Mapping(target = "eventId", source = "eventDto.id")
    @Mapping(target = "authorId", source = "userDto.id")
    Comment toComment(NewCommentDto newCommentDto, EventFullDto eventDto, UserDto userDto);

    @Mapping(target = "updated", expression = "java(LocalDateTime.now())")
    void updateCommentStatusFromDto(UpdateCommentDto commentDto, @MappingTarget Comment comment);

    @Mapping(target = "updated", expression = "java(LocalDateTime.now())")
    void updateCommentTextFromDto(NewCommentDto commentDto, @MappingTarget Comment comment);

}
