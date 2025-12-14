package ru.practicum.explorewithme.event.client.category;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.category.service.CategoryServiceApi;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class CategoryClientFallback implements CategoryServiceApi {

    @Override
    public List<ResponseCategoryDto> getAllByIds(Set<@Positive Long> ids) {
        log.warn("Сервис Category недоступен, fallback вернул пустой список для ids: {}", ids);
        return List.of();
    }

    @Override
    public ResponseCategoryDto getById(Long catId) {
        log.warn("Сервис Category недоступен, fallback вернул null для id: {}", catId);
        return null;
    }
}
