package ru.practicum.explorewithme.event.enums;

import org.springframework.data.domain.Sort;

public enum EventsSort {
    EVENT_DATE("eventDate", Sort.Direction.ASC),
    VIEWS("views", Sort.Direction.DESC);

    private final String name;
    private final Sort.Direction direction;

    EventsSort(String name, Sort.Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public Sort getSort() {
        return Sort.by(this.direction, this.name);
    }
}
