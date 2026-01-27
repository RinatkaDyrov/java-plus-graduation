package ru.practicum.service;

import ru.practicum.dto.event.request.EventRequestDto;
import ru.practicum.dto.event.request.EventRequestUpdateDto;
import ru.practicum.dto.event.request.EventRequestUpdateResult;

import java.util.List;

public interface EventRequestService {
    List<EventRequestDto> getUsersRequests(Long userId);

    EventRequestDto createRequest(Long userId, Long eventId);

    EventRequestDto cancelRequest(Long userId, Long requestId);

    List<EventRequestDto> getAllByEventId(Long userId, Long eventId);

    EventRequestUpdateResult updateRequestState(Long userId, Long eventId, EventRequestUpdateDto updateDto);
}
