package ru.practicum.explorewithme.api.category.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;

import java.util.List;
import java.util.Set;

public interface CategoryServiceApi {
    String URL = "/categories";

    @GetMapping(path = URL + "/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ResponseCategoryDto> getAllByIds(@NotNull @RequestParam Set<@Positive Long> ids);

    @GetMapping(value = "/{catId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseCategoryDto getById(@Positive @PathVariable Long catId);
}
