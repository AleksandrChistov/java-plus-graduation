package ru.practicum.explorewithme.error;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ApiError {

    private HttpStatus status;
    private String message; // Сообщение для пользователя
    private String reason; // Сообщение для разработчика
    private String stackTrace; // Не для production

}
