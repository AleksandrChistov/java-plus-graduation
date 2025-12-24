package ru.practicum.explorewithme.event.client.category;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.event.error.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class CategoryClientFallback implements CategoryClient {

    @Override
    public List<ResponseCategoryDto> getAllByIds(Set<@Positive Long> ids) {
        log.warn("Сервис Category недоступен, fallback вернул пустой список для ids: {}", ids);
        return List.of();
    }

    @Override
    public ResponseCategoryDto getById(Long catId) {
        log.warn("Сервис Category недоступен, fallback кинул ServiceUnavailableException для id: {}", catId);
        throw new ServiceUnavailableException("Сервис Category недоступен");
    }
}
