package ru.practicum.explorewithme.shared.util;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.category.dao.CategoryRepository;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.shared.error.exception.NotFoundException;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class CategoryServiceUtil {

    public static ResponseCategoryDto getResponseCategoryDto(CategoryRepository categoryRepository, CategoryMapper categoryMapper, Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toCategoryDto)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    public static Map<Long, ResponseCategoryDto> getResponseCategoryDtoMap(CategoryRepository categoryRepository, CategoryMapper categoryMapper, Set<Long> categoriesIds) {
        return categoryRepository.findAllById(categoriesIds).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toMap(ResponseCategoryDto::getId, Function.identity()));
    }

}
