package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.model.State;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.user.model.UserDto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventShortDto {
    Long id;
    String title;
    String annotation;
    CategoryDto category;
    Boolean paid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    UserShortDto initiator;
    int views;
    int confirmedRequests;
    String description;
    int participantLimit;
    State state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime publishedOn;

    LocationDto location;
    Boolean requestModeration;

}
