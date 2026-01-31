package ru.practicum.feignClient.event;

import ru.practicum.dto.event.EventFullDto;

import java.util.Optional;

public class EventClientFallback implements EventClient {

    @Override
    public Optional<EventFullDto> getEventById(Long eventId) {
        return Optional.empty();
    }

    @Override
    public Boolean incrementConfirmedRequests(Long eventId, Integer increment) {
        return Boolean.FALSE;
    }

}
