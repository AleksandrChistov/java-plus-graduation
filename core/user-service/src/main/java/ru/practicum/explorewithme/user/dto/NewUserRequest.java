package ru.practicum.explorewithme.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    @NotBlank
    @Size(max = 250, min = 2)
    private String name;
    @Email
    @NotBlank
    @Size(max = 254, min = 6)
    private String email;
}
