package ru.practicum.explorewithme.comment.dto;

import lombok.*;
import ru.practicum.explorewithme.comment.enums.Status;

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
