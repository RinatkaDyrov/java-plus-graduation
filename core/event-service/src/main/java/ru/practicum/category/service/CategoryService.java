package ru.practicum.category.service;

import ru.practicum.dto.category.CategoryCreateDto;
import ru.practicum.dto.category.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(CategoryCreateDto dto);

    CategoryDto update(Long catId, CategoryDto dto);

    void delete(Long catId);

    List<CategoryDto> findAll(int from, int size);

    CategoryDto findById(Long id);

}

