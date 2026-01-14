package ru.practicum.compilation.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationRequestDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @RequestParam(defaultValue = "0") @Min(0) int from,
                                                   @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        log.info("Получение подборки событий");
        CompilationRequestDto requestDto = CompilationRequestDto.builder()
                .pinned(pinned)
                .from(from)
                .size(size)
                .build();
        return compilationService.getAll(requestDto);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable @Positive Long compId) {
        log.info("Получение подборки событий по ID: {}", compId);
        return compilationService.getById(compId);
    }
}
