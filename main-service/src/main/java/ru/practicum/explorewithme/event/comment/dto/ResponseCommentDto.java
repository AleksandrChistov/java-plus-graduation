package ru.practicum.explorewithme.event.comment.dto;

import lombok.*;
import ru.practicum.explorewithme.event.comment.enums.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCommentDto {
    private Long id;
    private String text;
    private Long eventId;
    private Long authorId;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Status status;
}
