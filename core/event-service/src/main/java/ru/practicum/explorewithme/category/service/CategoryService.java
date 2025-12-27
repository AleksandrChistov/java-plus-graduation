package ru.practicum.explorewithme.category.service;

import ru.practicum.explorewithme.api.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.api.category.service.CategoryServiceApi;
import ru.practicum.explorewithme.category.dto.RequestCategoryDto;

import java.util.List;

public interface CategoryService extends CategoryServiceApi {

    List<ResponseCategoryDto> getCategories(int from, int size);

    ResponseCategoryDto save(RequestCategoryDto categoryDto);

    ResponseCategoryDto update(long catId, RequestCategoryDto categoryDto);

    void delete(long catId);

}
