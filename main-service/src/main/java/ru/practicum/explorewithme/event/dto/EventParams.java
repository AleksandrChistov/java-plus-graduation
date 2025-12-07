package ru.practicum.explorewithme.event.dto;

import lombok.*;
import ru.practicum.explorewithme.event.enums.EventsSort;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventParams {
    String text;
    List<Long> categories;
    Boolean paid;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    Boolean onlyAvailable;
    EventsSort eventsSort;
    int from;
    int size;
}
