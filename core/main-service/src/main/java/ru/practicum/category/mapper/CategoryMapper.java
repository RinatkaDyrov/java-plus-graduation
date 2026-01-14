package ru.practicum.category.mapper;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.dto.CategoryCreateDto;

public class CategoryMapper {
    public static CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toEntity(CategoryCreateDto dto) {
        return Category.builder().name(dto.getName()).build();
    }

    public static void update(Category category, CategoryDto dto) {
        category.setName(dto.getName());
    }
}
