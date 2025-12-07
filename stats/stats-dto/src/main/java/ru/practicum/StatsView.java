package ru.practicum;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatsView {
    private String app;
    private String uri;
    private Long hits;
}
