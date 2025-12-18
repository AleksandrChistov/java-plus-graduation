package ru.practicum.explorewithme.category.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.category.service.CategoryServiceApi;
import ru.practicum.explorewithme.category.service.CategoryService;

import java.util.List;
import java.util.Set;

@RestController
@Validated
@RequiredArgsConstructor
public class CategoryController implements CategoryServiceApi {

    private final CategoryService categoryService;

    @GetMapping(path = CategoryServiceApi.URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseCategoryDto> getAll(
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return categoryService.getCategories(from, size);
    }

    @Override
    public ResponseCategoryDto getById(Long catId) {
        return categoryService.getById(catId);
    }

    @Override
    public List<ResponseCategoryDto> getAllByIds(Set<Long> ids) {
        return categoryService.getAllByIds(ids);
    }
}
