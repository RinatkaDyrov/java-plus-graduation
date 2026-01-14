package ru.practicum.eventRequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.eventRequest.model.Status;

import java.time.LocalDateTime;

@Data
@Builder
public class EventRequestDto {
    private Long id;
    private Long event;
    private Long requester;
    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
}
