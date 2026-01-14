package ru.practicum.eventRequest.mapper;

import ru.practicum.eventRequest.dto.EventRequestDto;
import ru.practicum.eventRequest.model.EventRequest;

public class EventRequestMapper {

    public static EventRequestDto mapToEventRequestDto(EventRequest eventRequest) {
        return EventRequestDto.builder()
                .id(eventRequest.getId())
                .requester(eventRequest.getRequester().getId())
                .event(eventRequest.getEvent().getId())
                .status(eventRequest.getStatus())
                .created(eventRequest.getCreated())
                .build();
    }
}
