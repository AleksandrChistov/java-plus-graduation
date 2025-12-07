package ru.practicum.explorewithme.event.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.explorewithme.event.comment.enums.Status;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentDto {
    @NotNull(message = "Status can't be null")
    private Status status;
}
