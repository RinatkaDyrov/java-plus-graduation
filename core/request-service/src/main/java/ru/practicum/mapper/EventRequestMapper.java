package ru.practicum.mapper;

import ru.practicum.dto.event.request.EventRequestDto;
import ru.practicum.model.EventRequest;

public class EventRequestMapper {

    public static EventRequestDto mapToEventRequestDto(EventRequest eventRequest) {
        return EventRequestDto.builder()
                .id(eventRequest.getId())
                .requester(eventRequest.getRequesterId())
                .event(eventRequest.getEventId())
                .status(eventRequest.getStatus())
                .created(eventRequest.getCreated())
                .build();
    }
}
