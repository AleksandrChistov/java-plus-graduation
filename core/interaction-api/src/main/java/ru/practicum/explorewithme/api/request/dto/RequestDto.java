package ru.practicum.explorewithme.api.request.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private Long id;
    private Instant created;
    private Long event;
    private Long requester;
    private String status;
}
