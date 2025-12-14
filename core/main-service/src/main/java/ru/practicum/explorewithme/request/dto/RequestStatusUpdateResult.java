package ru.practicum.explorewithme.request.dto;

import lombok.*;
import ru.practicum.explorewithme.api.request.dto.RequestDto;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusUpdateResult {
    private List<RequestDto> confirmedRequests;
    private List<RequestDto> rejectedRequests;
}
