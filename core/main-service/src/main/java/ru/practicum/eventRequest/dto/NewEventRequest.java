package ru.practicum.eventRequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.location.dto.NewLocationRequest;
import ru.practicum.validator.ValidEventDate;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventRequest {
    @NotBlank
    @Size(min = 3, max = 120)
    String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;

    @NotBlank
    @Size(min = 20, max = 7000)
    String description;

    @NotNull
    Long category;

    @NotNull
    @ValidEventDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @NotNull
    NewLocationRequest location;

    Boolean paid = false;

    @PositiveOrZero
    Integer participantLimit;

    Boolean requestModeration = true;

    //метод проверки наличия поля, иначе ставим 0
    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewEventRequest that = (NewEventRequest) o;
        return Objects.equals(participantLimit, that.participantLimit) && Objects.equals(title, that.title)
                && Objects.equals(annotation, that.annotation) && Objects.equals(description, that.description)
                && Objects.equals(category, that.category) && Objects.equals(eventDate, that.eventDate)
                && Objects.equals(location, that.location) && Objects.equals(paid, that.paid)
                && Objects.equals(requestModeration, that.requestModeration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, annotation, description, category, eventDate, location, paid,
                participantLimit, requestModeration);
    }

    @Override
    public String toString() {
        return "NewEventRequest{" +
                "title='" + title + '\'' +
                ", annotation='" + annotation + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", eventDate=" + eventDate +
                ", location=" + location +
                ", paid=" + paid +
                ", participantLimit=" + participantLimit +
                ", requestModeration=" + requestModeration +
                '}';
    }
}
