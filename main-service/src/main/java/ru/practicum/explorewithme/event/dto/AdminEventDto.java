package ru.practicum.explorewithme.event.dto;

import lombok.*;
import ru.practicum.explorewithme.event.enums.State;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminEventDto {

    List<Long> users;

    List<State> states;

    List<Long> categories;

    LocalDateTime rangeStart;

    LocalDateTime rangeEnd;

    Long from;

    Long size;

}
