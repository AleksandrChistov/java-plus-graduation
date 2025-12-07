package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static ru.practicum.StatsUtil.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatsDto {
    @NotBlank(message = "App name cannot be blank")
    private String app;
    @NotBlank(message = "URI cannot be blank")
    private String uri;
    @NotBlank(message = "IP address cannot be blank")
    @Pattern(regexp = IP_ADDRESS_PATTERN,
            message = "Invalid IP address format")
    private String ip;
    @NotNull(message = "Timestamp cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;
}
