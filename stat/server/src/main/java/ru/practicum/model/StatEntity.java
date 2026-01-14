package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatEntity {
    private String app;
    private String uri;
    private Long hits;
}

