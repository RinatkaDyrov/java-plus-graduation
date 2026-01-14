package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventSearchParam {
    String text;
    List<Long> users;
    List<String> states;
    List<Long> categories;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    Boolean paid;
    Boolean onlyAvailable;
    String sort;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSearchParam that = (EventSearchParam) o;
        return Objects.equals(text, that.text) && Objects.equals(users, that.users)
                && Objects.equals(states, that.states) && Objects.equals(categories, that.categories)
                && Objects.equals(rangeStart, that.rangeStart) && Objects.equals(rangeEnd, that.rangeEnd)
                && Objects.equals(paid, that.paid) && Objects.equals(onlyAvailable, that.onlyAvailable)
                && Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, users, states, categories, rangeStart, rangeEnd, paid, onlyAvailable, sort);
    }
}
