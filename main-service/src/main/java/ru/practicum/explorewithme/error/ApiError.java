package ru.practicum.explorewithme.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ApiError {

    private HttpStatus status;
    private String reason; // Общее описание причины ошибки - для разработчика
    private String message; // Сообщение об ошибке - для пользователя
    private String timestamp; // Дата и время когда произошла ошибка (в формате "yyyy-MM-dd HH:mm:ss")
    private String errors; // Не для production

}
