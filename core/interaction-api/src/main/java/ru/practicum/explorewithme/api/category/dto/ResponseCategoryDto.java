package ru.practicum.explorewithme.api.category.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCategoryDto {
    private Long id;
    private String name;
}
