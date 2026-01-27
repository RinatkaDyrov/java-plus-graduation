package ru.practicum.dto.event.request;

import lombok.*;

import java.util.List;

@Builder
@Data
public class EventRequestUpdateResult {
    List<EventRequestDto> confirmedRequests;
    List<EventRequestDto> rejectedRequests;
}

