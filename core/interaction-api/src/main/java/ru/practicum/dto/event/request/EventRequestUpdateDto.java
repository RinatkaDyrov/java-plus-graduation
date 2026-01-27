package ru.practicum.dto.event.request;

import lombok.*;

import java.util.List;

@Builder
@Data
public class EventRequestUpdateDto {
    List<Long> requestIds;
    String status;
}
