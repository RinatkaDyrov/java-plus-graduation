package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommentUpdateRequestDto {
    @NotNull
    List<Long> commentIds;

    @NotNull
    String state;
}
