package ru.practicum.dto.compilation;

import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationRequest {
    List<Long> events;
    Boolean pinned;
    String title;
}
