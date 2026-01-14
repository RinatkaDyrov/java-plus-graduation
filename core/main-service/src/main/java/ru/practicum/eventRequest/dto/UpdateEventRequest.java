package ru.practicum.eventRequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.location.dto.NewLocationRequest;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventRequest {
    @Size(min = 20, max = 2000)
    String annotation;
    Integer category;
    @Size(min = 20, max = 7000)
    String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    NewLocationRequest location;
    Boolean paid;
    Integer participantLimit;
    Boolean requestModeration;
    String stateAction;

    @Size(min = 3, max = 120)
    String title;
}
