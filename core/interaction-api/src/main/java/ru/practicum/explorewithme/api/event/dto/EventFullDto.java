package ru.practicum.explorewithme.api.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.explorewithme.api.shared.util.ConstantUtil.DATE_TIME_FORMAT;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private ResponseCategoryDto category;
    private UserShortDto initiator;
    private Boolean paid;
    private Boolean requestModeration;
    private Integer participantLimit;
    private LocationDto location;
    private String state;
    private Double rating;
    private Long confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime publishedOn;
}
