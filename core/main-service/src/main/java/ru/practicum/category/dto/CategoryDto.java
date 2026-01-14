package ru.practicum.category.dto;

//потом перенесем в отдельный модуль все дто

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    @Size(min = 1, max = 50)
    @NotBlank
    private String name;
}
