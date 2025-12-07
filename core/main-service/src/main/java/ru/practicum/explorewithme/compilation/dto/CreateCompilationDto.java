package ru.practicum.explorewithme.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompilationDto {
    @NotBlank
    @Size(max = 50)
    private String title;
    @Builder.Default
    private Boolean pinned = false;
    private Set<Long> events;
}
