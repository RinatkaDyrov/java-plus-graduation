package ru.practicum.dto.comment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentSearchParam {
    List<String> states;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
}
