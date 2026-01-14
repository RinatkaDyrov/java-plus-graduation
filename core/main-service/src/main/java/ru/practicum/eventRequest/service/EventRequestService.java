package ru.practicum.eventRequest.service;

import ru.practicum.eventRequest.dto.EventRequestDto;
import ru.practicum.eventRequest.dto.EventRequestUpdateDto;
import ru.practicum.eventRequest.dto.EventRequestUpdateResult;

import java.util.List;

public interface EventRequestService {
    List<EventRequestDto> getUsersRequests(Long userId);

    EventRequestDto createRequest(Long userId, Long eventId);

    EventRequestDto cancelRequest(Long userId, Long requestId);

    List<EventRequestDto> getAllByEventId(Long userId, Long eventId);

    EventRequestUpdateResult updateRequestState(Long userId, Long eventId, EventRequestUpdateDto updateDto);
}
