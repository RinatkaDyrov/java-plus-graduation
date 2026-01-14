package ru.practicum.compilation.mapper;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;

public class CompilationMapper {
    public static CompilationDto mapToDto(Compilation compilation) {
        return CompilationDto.builder()
                .events(compilation.getEvents().stream()
                        .map(EventMapper::mapToShortDto)
                        .toList())
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static Compilation mapToCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .build();
    }

}
