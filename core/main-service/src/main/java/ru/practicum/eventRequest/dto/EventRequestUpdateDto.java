package ru.practicum.eventRequest.dto;

import lombok.*;

import java.util.List;

@Builder
@Data
public class EventRequestUpdateDto {
    List<Long> requestIds;
    String status;
}
