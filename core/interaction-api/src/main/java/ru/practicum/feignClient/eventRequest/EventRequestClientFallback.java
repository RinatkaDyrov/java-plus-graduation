package ru.practicum.feignClient.eventRequest;

import ru.practicum.dto.event.request.EventRequestDto;

import java.util.Optional;

public class EventRequestClientFallback implements EventRequestClient {
    @Override
    public Optional<EventRequestDto> getByEventIdAndRequesterId(Long eventId, Long userId) {
        return Optional.empty();
    }
}
