package ru.practicum.explorewithme.category.service;

import ru.practicum.explorewithme.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.category.dto.RequestCategoryDto;

import java.util.List;

public interface CategoryService {

    List<ResponseCategoryDto> getCategories(int from, int size);

    ResponseCategoryDto getCategory(long catId);

    ResponseCategoryDto save(RequestCategoryDto categoryDto);

    ResponseCategoryDto update(long catId, RequestCategoryDto categoryDto);

    void delete(long catId);

}
