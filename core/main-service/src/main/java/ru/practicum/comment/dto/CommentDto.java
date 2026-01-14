package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.State;
import ru.practicum.helper.RequestParamHelper;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CommentDto {
    Long id;
    String text;

    @JsonFormat(pattern = RequestParamHelper.DATE_TIME_FORMAT)
    LocalDateTime created;

    String creatorName;
    String eventName;
    State state;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentDto that = (CommentDto) o;
        return Objects.equals(id, that.id) && Objects.equals(text, that.text) && Objects.equals(created, that.created)
                && Objects.equals(creatorName, that.creatorName) && Objects.equals(eventName, that.eventName)
                && state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, created, creatorName, eventName, state);
    }
}
