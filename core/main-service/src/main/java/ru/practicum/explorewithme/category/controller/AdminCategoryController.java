package ru.practicum.explorewithme.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.category.dto.RequestCategoryDto;
import ru.practicum.explorewithme.category.service.CategoryService;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = AdminCategoryController.URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminCategoryController {

    public static final String URL = "/admin/categories";

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCategoryDto create(@Valid @RequestBody RequestCategoryDto categoryDto) {
        return categoryService.save(categoryDto);
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseCategoryDto update(
            @Positive @PathVariable Long catId,
            @Valid @RequestBody RequestCategoryDto categoryDto
    ) {
        return categoryService.update(catId, categoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long catId) {
        categoryService.delete(catId);
    }

}
