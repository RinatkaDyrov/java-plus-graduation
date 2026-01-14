package ru.practicum.eventRequest.dto;

import lombok.*;

import java.util.List;

@Builder
@Data
public class EventRequestUpdateResult {
    List<EventRequestDto> confirmedRequests;
    List<EventRequestDto> rejectedRequests;
}

