package ru.practicum.category.dto;

//потом перенесем в отдельный модуль все дто
// в спеке это NewCategoryDto

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}
