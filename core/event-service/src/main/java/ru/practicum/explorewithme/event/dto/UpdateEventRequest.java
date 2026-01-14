package ru.practicum.explorewithme.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.explorewithme.api.event.dto.LocationDto;
import ru.practicum.explorewithme.event.enums.StateAction;

import java.time.LocalDateTime;

import static ru.practicum.explorewithme.api.shared.util.ConstantUtil.DATE_TIME_FORMAT;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {
    @Size(max = 2000, min = 20)
    private String annotation;
    private Long category;
    @Size(max = 7000, min = 20)
    private String description;
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
    @Size(max = 120, min = 3)
    private String title;
}
