package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.dto.CategoryCreateDto;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exeption.ConflictException;
import ru.practicum.exeption.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto create(CategoryCreateDto dto) {
        if (repository.existsByName(dto.getName())) {
            throw new ConflictException("Category name must be unique");
        }
        return CategoryMapper.mapToDto(repository.save(CategoryMapper.toEntity(dto)));
    }

    @Override
    @Transactional
    public CategoryDto update(Long catId, CategoryDto dto) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
        if (!category.getName().equals(dto.getName()) && repository.existsByName(dto.getName())) {
            throw new ConflictException("Category name must be unique");
        }
        CategoryMapper.update(category, dto);
        return CategoryMapper.mapToDto(repository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("The category is not empty");
        }

        repository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repository.findAll(pageable).stream()
                .map(CategoryMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return CategoryMapper.mapToDto(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found")));
    }
}

